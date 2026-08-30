package org.openntf.drapi.internal.http.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openntf.drapi.internal.http.HttpHeaderNames.USER_AGENT;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.HttpMethod;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.internal.test.AbstractHttpMockTest;

class JdkHttpTransportTest extends AbstractHttpMockTest {

    protected HttpTransport createTransport(DrapiConfig config) {
        return new JdkHttpTransport(config, null);
    }

    @Test
    @DisplayName("Basic GET request should be mirrored correctly and return expected response")
    void getRequestSendsPathAndMethod() {
        DrapiConfig config = buildConfig(null);
        respondWith(200, "Hello World", Map.of("X-Test-Header", List.of("some-value", "another-value")));

        try (var response = createTransport(config).submit(DrapiRequest.get("/test").queryParam("p1", "v1").queryParam("p2", "v2 v3"))) {
            DrapiRequest mirroredRequest = mirrorRequest.get();

            assertEquals("/test", mirroredRequest.path(), "The mirrored request path should match the original request path");
            assertTrue(mirroredRequest.containsQueryParam("p1", "v1"), "The mirrored request should contain query parameter p1=v1");
            assertTrue(mirroredRequest.containsQueryParam("p2", "v2 v3"), "The mirrored request should contain query parameter p2=v2 v3");
            assertEquals(HttpMethod.GET, mirroredRequest.httpMethod(), "The mirrored request HTTP method should match the original request HTTP method");
            assertTrue(mirroredRequest.containsHeader(USER_AGENT, config.userAgent()), "The mirrored request should have the correct User-Agent header");

            assertEquals(200, response.statusCode(), "The response status code should match the expected status code");
            assertEquals("Hello World", response.bodyAsString(), "The response body should match the expected body");
            assertTrue(response.containsHeader("X-Test-Header", "some-value"), "The mirrored request should have the correct X-Test-Header header");
            assertTrue(response.containsHeader("X-Test-Header", "another-value"), "The mirrored request should have the correct X-Test-Header header");
        }
    }

    @Test
    @DisplayName("Test that server returns 404 and we can detect that normally")
    void test404Response() {
        DrapiConfig config = buildConfig(null);
        respondWith(404, "Not Found");

        try (var response = createTransport(config).submit(DrapiRequest.get("/nonexistent"))) {
            assertEquals(404, response.statusCode(), "The response status code should be 404 for a nonexistent resource");
            assertEquals("Not Found", response.bodyAsString(), "The response body should indicate that the resource was not found");
            assertFalse(response.isSuccess(), "The response should not be considered successful for a 404 status code");
        }
    }

    @Test
    @DisplayName("Test that server returns 401 and we can detect that normally")
    void test401Response() {
        DrapiConfig config = buildConfig(null);
        respondWith(401, "Unauthorized");

        try (var response = createTransport(config).submit(DrapiRequest.get("/nonexistent"))) {
            assertEquals(401, response.statusCode(), "The response status code should be 401 for a nonexistent resource");
            assertFalse(response.isSuccess(), "The response should not be considered successful for a 401 status code");
            assertTrue(response.isAuthenticationFailure(), "The response should be considered an authentication failure for a 401 status code");
        }
    }

    @Test
    @DisplayName("Simulate unresponsive server and ensure that the transport handles it gracefully")
    void testUnresponsiveServer() {
        DrapiConfig config = buildConfig(builder -> {
            builder.baseUrl(URI.create("http://127.0.0.1:" + findUnusedPort()).toString()); // Use an unused port to simulate unresponsiveness
        });
        respondWith(200, "Successful Response"); // This response will never be reached due to the unused port

        try (var response = createTransport(config).submit(DrapiRequest.get("/test"))) {
            throw new AssertionError("Expected an exception due to unresponsive server, but got a response: " + response.statusCode());
        } catch (RuntimeException e) {
            // We expect a RuntimeException due to the unresponsive server. The exact exception type may vary based on the underlying HTTP client implementation.
            assertInstanceOf(RuntimeException.class, e,
                             "Expected a RuntimeException due to unresponsive server, but got: " + e.getCause());

            // TODO Revisit this after we have a more robust exception handling mechanism in place. For now, we just check that an exception was thrown.
        }
    }

}
