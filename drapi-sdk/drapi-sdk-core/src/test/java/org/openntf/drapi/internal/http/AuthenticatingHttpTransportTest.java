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
package org.openntf.drapi.internal.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.auth.SessionContext;
import org.openntf.drapi.auth.Token;
import org.openntf.drapi.auth.TokenSourceBase;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.http.HttpMethod;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.http.HttpTransportBase;
import org.openntf.drapi.http.HttpTransportProvider;
import org.openntf.drapi.internal.test.MockableHttpTest;
import org.openntf.drapi.util.CloseTrackingInputStream;

@ExtendWith(MockitoExtension.class)
class AuthenticatingHttpTransportTest extends MockableHttpTest {

    @Mock
    TokenSourceBase tokenSource;

    @Mock
    Responder responder;

    @BeforeEach
    void setup() {
        respondWith(responder);
    }

    protected HttpTransport createTransport(DrapiConfig config) {
        HttpTransport bareTransport = HttpTransportProvider.defaultTransportProvider()
                                                           .create(config, null);

        return new AuthenticatingHttpTransport(bareTransport, tokenSource);
    }

    protected DrapiRequest createRequest(HttpMethod method, String path) {
        return DrapiRequest.create(method, path)
                           .sessionContext(SessionContext.singleUser());
    }

    @Test
    @DisplayName("Test that the AuthenticatingHttpTransport can handle a simple authentication flow")
    void simpleAuthenticationFlowWithToken() {
        DrapiConfig config = buildConfig(null);
        HttpTransport transport = createTransport(config);

        when(responder.respond(any())).thenReturn(
            response(200, "Success")
        );

        // Auth provider automatically returns a token when acquireToken is called
        when(tokenSource.acquire(any())).thenReturn(new Token("test-token", "test"));

        try (var response = transport.submit(createRequest(HttpMethod.GET, "/test"))) {
            assertEquals(1, requestCount.get(), "The mirror server should have received exactly one request");
            assertTrue(mirrorRequest.get()
                                    .containsHeader("Authorization", "Bearer test-token"), "The mirrored request should contain the correct Authorization header");
            assertEquals(200, response.statusCode(), "The response status code should match the expected status code");
        }
    }

    @Test
    @DisplayName("Test that the AuthenticatingHttpTransport fails with non-repeatable authentication provider after a 401 response")
    void nonRepeatableAuthenticationProviderFailsAfter401() {
        DrapiConfig config = buildConfig(null);
        HttpTransport transport = createTransport(config);

        when(responder.respond(any())).thenReturn(
            response(401, """
                {
                    "status": 401,
                    "message": "Unwelcome visitor! We’re boiling the tar, so be careful!",
                    "errorId": 9999,
                    "details": "Unauthorized"
                }
                """)
        );

        // Auth provider does not support refresh, so it should not retry after a 401 response
        when(tokenSource.supportsRefresh()).thenReturn(false);

        // Auth provider automatically returns a token when acquireToken is called
        when(tokenSource.acquire(any())).thenReturn(new Token("test-token", "test"));

        try (var response = transport.submit(createRequest(HttpMethod.GET, "/test"))) {
            assertEquals(1, requestCount.get(), "The mirror server should have received exactly one request");
            assertEquals(401, response.statusCode(), "The response status code should match the expected status code");
        }
    }

    @Test
    @DisplayName("Test that the AuthenticatingHttpTransport fails with repeatable authentication provider after a 401 response")
    void repeatableAuthenticationProviderFailsAfter401() {

        DrapiConfig config = buildConfig(null);
        HttpTransport transport = createTransport(config);

        when(responder.respond(any())).thenReturn(
            response(401, """
                {
                    "status": 401,
                    "message": "Unwelcome visitor! We’re boiling the tar, so be careful!",
                    "errorId": 9999,
                    "details": "Unauthorized"
                }
                """)
        );

        // Auth provider supports refresh, so it should retry after a 401 response
        when(tokenSource.supportsRefresh()).thenReturn(true);

        // Auth provider automatically returns a token when acquireToken is called
        when(tokenSource.acquire(any())).thenReturn(new Token("test-token", "test"));

        try (var response = transport.submit(createRequest(HttpMethod.GET, "/test"))) {
            assertEquals(2, requestCount.get(), "The mirror server should have received exactly two requests");
            assertEquals(401, response.statusCode(), "The response status code should match the expected status code");
        }
    }

    @Test
    @DisplayName("Test that the AuthenticatingHttpTransport retries with repeatable authentication provider after a 401 response")
    void repeatableAuthenticationProviderRetriesAfter401() {
        DrapiConfig config = buildConfig(null);
        HttpTransport transport = createTransport(config);

        // First response is 401, second response is 200
        when(responder.respond(any())).thenReturn(
            response(401, """
                {
                    "status": 401,
                    "message": "Unwelcome visitor! We’re boiling the tar, so be careful!",
                    "errorId": 9999,
                    "details": "Unauthorized"
                }
                """),
            response(200, "Success")
        );

        // Auth provider supports refresh, so it should retry after a 401 response
        when(tokenSource.supportsRefresh()).thenReturn(true);

        // Auth provider automatically returns a token when acquireToken is called
        when(tokenSource.acquire(any())).thenReturn(new Token("test-token", "test"));

        try (var response = transport.submit(createRequest(HttpMethod.GET, "/test"))) {
            assertEquals(2, requestCount.get(), "The mirror server should have received exactly two requests");
            assertEquals(200, response.statusCode(), "The response status code should match the expected status code");
            assertTrue(mirrorRequest.get()
                                    .containsHeader("Authorization", "Bearer test-token"), "The mirrored request should contain the correct Authorization header");
        }
    }

    // Added to validate an issue came up with Claude code-review
    @Test
    @DisplayName("A rejected 401 response should be closed before the request is retried")
    void rejectedResponseIsClosedBeforeRetry() {
        DrapiConfig config = buildConfig(null);

        // A scripted transport: the first call answers 401, every later call answers 200. Each body records whether it was closed.                                                                                  
        List<CloseTrackingInputStream> bodies = new ArrayList<>();
        HttpTransport transport = createCountingTransport(config, bodies);

        when(tokenSource.supportsRefresh()).thenReturn(true);
        when(tokenSource.acquire(any())).thenReturn(new Token("test-token", "test"));

        try (var response = transport.submit(createRequest(HttpMethod.GET, "/test"))) {
            assertEquals(200, response.statusCode(), "The retried request should succeed");
            assertEquals(2, bodies.size(), "The request should have been sent twice");
            assertTrue(bodies.get(0).isClosed(), "The rejected 401 response should be closed, otherwise its connection is never released");
            assertFalse(bodies.get(1).isClosed(), "The response handed to the caller should still be open");
        }
    }

    private HttpTransport createCountingTransport(DrapiConfig config, List<CloseTrackingInputStream> bodies) {
        HttpTransport scriptedTransport = new HttpTransportBase(config, null) {
            @Override
            public CompletableFuture<DrapiResponse> submitAsync(DrapiRequest request) {
                int status = bodies.isEmpty() ? 401 : 200;
                var body = new CloseTrackingInputStream(status == 401 ? "Unauthorized" : "OK");
                bodies.add(body);
                return CompletableFuture.completedFuture(new DrapiResponse(status, Map.of(), body));
            }
        };

        return new AuthenticatingHttpTransport(scriptedTransport, tokenSource);
    }

}
