/*
 * Copyright (c) 2026 Serdar Basegmez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.drapi.internal.http.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.openntf.drapi.http.HttpMethod.GET;
import static org.openntf.drapi.http.HttpMethod.POST;
import static org.openntf.drapi.internal.http.HttpHeaderConstants.USER_AGENT;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.exception.HttpTransportException;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.HttpMethod;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.http.RequestBody;
import org.openntf.drapi.http.RequestBody.Bytes;
import org.openntf.drapi.internal.test.MockableHttpTest;
import org.openntf.drapi.internal.test.TestUtils;

@ExtendWith(MockitoExtension.class)
class JdkHttpTransportTest extends MockableHttpTest {

    @Mock
    Responder responder;

    @BeforeEach
    void setup() {
        respondWith(responder);
    }

    protected HttpTransport createTransport(DrapiConfig config) {
        return new JdkHttpTransport(config, null);
    }

    @Test
    @DisplayName("Basic GET request should be mirrored correctly and return expected response")
    void getRequestSendsPathAndMethod() {
        var config = buildConfig(null);
        var transport = createTransport(config);

        when(responder.respond(any())).thenReturn(
            response(200, "Hello World", Map.of("X-Test-Header", List.of("some-value", "another-value")))
        );

        var request = DrapiRequest.create(GET, "/test")
                                  .queryParam("p1", "v1")
                                  .queryParam("p2", "v2 v3");

        try (var response = transport.submit(request)) {
            DrapiRequest mirroredRequest = mirrorRequest.get();

            assertEquals("/test", mirroredRequest.path(), "The mirrored request path should match the original request path");
            assertTrue(mirroredRequest.containsQueryParam("p1", "v1"), "The mirrored request should contain query parameter p1=v1");
            assertTrue(mirroredRequest.containsQueryParam("p2", "v2 v3"), "The mirrored request should contain query parameter p2=v2 v3");
            assertEquals(GET, mirroredRequest.httpMethod(), "The mirrored request HTTP method should match the original request HTTP method");
            assertTrue(mirroredRequest.containsHeader(USER_AGENT, config.userAgent()), "The mirrored request should have the correct User-Agent header");

            assertEquals(200, response.statusCode(), "The response status code should match the expected status code");
            assertEquals("Hello World", response.bodyAsString(), "The response body should match the expected body");
            assertTrue(response.containsHeader("X-Test-Header", "some-value"), "The mirrored request should have the correct X-Test-Header header");
            assertTrue(response.containsHeader("X-Test-Header", "another-value"), "The mirrored request should have the correct X-Test-Header header");
        }
    }

    @Test
    @DisplayName("Test POST request with body and headers, ensuring the request is mirrored correctly and response is as expected")
    void postRequestWithBodyAndHeaders() {
        var config = buildConfig(null);

        when(responder.respond(any())).thenReturn(
            response(201, "Created")
        );

        var request = DrapiRequest.create(POST, "/create")
                                  .header("Content-Type", "text/plain", false)
                                  .body(RequestBody.ofString("application/json", "{\"name\":\"test\"}"));

        try (var response = createTransport(config).submit(request)) {
            DrapiRequest mirroredRequest = mirrorRequest.get();

            assertEquals("/create", mirroredRequest.path(), "The mirrored request path should match the original request path");
            assertEquals(HttpMethod.POST, mirroredRequest.httpMethod(), "The mirrored request HTTP method should match the original request HTTP method");
            assertTrue(mirroredRequest.containsHeader("Content-Type", "application/json"), "The mirrored request should have the correct Content-Type header");
            assertFalse(mirroredRequest.containsHeader("Content-Type", "text/plain"), "The mirrored request should not have the incorrect Content-Type header");

            String body = new String(((Bytes) mirroredRequest.body()).data(), StandardCharsets.UTF_8);
            assertEquals("{\"name\":\"test\"}", body, "The mirrored request body should match the original request body");

            assertEquals(201, response.statusCode(), "The response status code should match the expected status code");
            assertEquals("Created", response.bodyAsString(), "The response body should match the expected body");
            assertEquals("Created", response.bodyAsString(), "The response body should be cached for multiple reads");
        }
    }


    @Test
    @DisplayName("Test that server returns 404 and we can detect that normally")
    void test404Response() {
        var config = buildConfig(null);
        var transport = createTransport(config);

        when(responder.respond(any())).thenReturn(
            response(404, "Not Found")
        );

        var request = DrapiRequest.create(GET, "/nonexistent");

        try (var response = transport.submit(request)) {
            assertEquals(404, response.statusCode(), "The response status code should be 404 for a nonexistent resource");
            assertEquals("Not Found", response.bodyAsString(), "The response body should indicate that the resource was not found");
            assertFalse(response.isSuccess(), "The response should not be considered successful for a 404 status code");
        }
    }

    @Test
    @DisplayName("Test that server returns 401 and we can detect that normally")
    void test401Response() {
        var config = buildConfig(null);
        var transport = createTransport(config);

        when(responder.respond(any())).thenReturn(
            response(401, "Unauthorized")
        );

        var request = DrapiRequest.create(GET, "/nonexistent");

        try (var response = transport.submit(request)) {
            assertEquals(401, response.statusCode(), "The response status code should be 401 for a nonexistent resource");
            assertFalse(response.isSuccess(), "The response should not be considered successful for a 401 status code");
            assertTrue(response.isAuthenticationFailure(), "The response should be considered an authentication failure for a 401 status code");
        }
    }

    @Test
    @DisplayName("Simulate unresponsive server and ensure that the transport handles it gracefully")
    void testUnresponsiveServer() {

        // Use an unused port to simulate unresponsiveness
        var url = URI.create("http://127.0.0.1:" + TestUtils.findUnusedPort()).toString();
        var config = buildConfig(builder -> builder.baseUrl(url));
        var transport = createTransport(config);

        assertThrows(HttpTransportException.class, () -> {
            try (var response = transport.submit(DrapiRequest.create(GET, "/test"))) {
                // This line should not be reached due to the unresponsive server
                response.bodyAsString();
            }
        }, "Expected a HttpTransportException due to unresponsive server, but no exception was thrown.");
    }

}
