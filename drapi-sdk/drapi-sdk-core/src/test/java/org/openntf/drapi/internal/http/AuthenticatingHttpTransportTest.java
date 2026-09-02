package org.openntf.drapi.internal.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.internal.auth.AuthenticationToolkit;
import org.openntf.drapi.internal.auth.BearerToken;
import org.openntf.drapi.internal.auth.TokenAuthenticationProvider;
import org.openntf.drapi.internal.test.MockableHttpTest;

@ExtendWith(MockitoExtension.class)
class AuthenticatingHttpTransportTest extends MockableHttpTest {

    @Mock
    TokenAuthenticationProvider authProvider;

    @Mock
    Responder responder;

    @BeforeEach
    void setup() {
        respondWith(responder);
    }

    protected HttpTransport createTransport(DrapiConfig config) {
        HttpTransport bareTransport = HttpTransport.defaultTransport(config, null);

        AuthenticationToolkit toolkit = new AuthenticationToolkit(bareTransport);
        return new AuthenticatingHttpTransport(toolkit, authProvider);
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
        when(authProvider.acquireToken(any())).thenReturn(new BearerToken("test-token", Map.of()));

        try (var response = transport.submit(DrapiRequest.get("/test"))) {

            assertEquals(1, requestCount.get(), "The mirror server should have received exactly one request");
            assertTrue(mirrorRequest.get().containsHeader("Authorization", "Bearer test-token"), "The mirrored request should contain the correct Authorization header");
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
        when(authProvider.supportsRefresh()).thenReturn(false);

        // Auth provider automatically returns a token when acquireToken is called
        when(authProvider.acquireToken(any())).thenReturn(new BearerToken("test-token", Map.of()));

        try (var response = transport.submit(DrapiRequest.get("/test"))) {
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
        when(authProvider.supportsRefresh()).thenReturn(true);

        // Auth provider automatically returns a token when acquireToken is called
        when(authProvider.acquireToken(any())).thenReturn(new BearerToken("test-token", Map.of()));

        try (var response = transport.submit(DrapiRequest.get("/test"))) {
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
        when(authProvider.supportsRefresh()).thenReturn(true);

        // Auth provider automatically returns a token when acquireToken is called
        when(authProvider.acquireToken(any())).thenReturn(new BearerToken("test-token", Map.of()));

        try (var response = transport.submit(DrapiRequest.get("/test"))) {
            assertEquals(2, requestCount.get(), "The mirror server should have received exactly two requests");
            assertEquals(200, response.statusCode(), "The response status code should match the expected status code");
            assertTrue(mirrorRequest.get().containsHeader("Authorization", "Bearer test-token"), "The mirrored request should contain the correct Authorization header");
        }
    }


}
