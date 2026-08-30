package org.openntf.drapi.internal.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.internal.auth.AuthenticationProvider;
import org.openntf.drapi.internal.auth.AuthenticationToolkit;
import org.openntf.drapi.internal.test.AbstractHttpMockTest;
import org.openntf.drapi.json.JsonBinding;

class AuthenticatingHttpTransportTest extends AbstractHttpMockTest {

    protected HttpTransport createTransport(DrapiConfig config) {
        JsonBinding jsonBinding = mock(JsonBinding.class);
        HttpTransport bareTransport = HttpTransport.defaultTransport(config, null);

        AuthenticationToolkit toolkit = new AuthenticationToolkit(bareTransport, jsonBinding);
        AuthenticationProvider authProvider = AuthenticationProvider.create(config);

        return new AuthenticatingHttpTransport(toolkit, authProvider);
    }

    @Test
    @DisplayName("Test that the AuthenticatingHttpTransport can handle a simple authentication flow with a token")
    void simpleAuthenticationFlowWithToken() {
        DrapiConfig config = buildConfig(builder -> builder.token("test-token"));

        respondWith(200, "Authenticated Response");

        try (var response = createTransport(config).submit(DrapiRequest.get("/test"))) {
            DrapiRequest mirroredRequest = mirrorRequest.get();

            assertEquals(1, requestCount.get(), "The mirror server should have received exactly one request");
            assertEquals("/test", mirroredRequest.path(), "The mirrored request path should match the original request path");
            assertTrue(mirroredRequest.containsHeader("Authorization", "Bearer test-token"), "The mirrored request should contain the correct Authorization header");
            assertEquals(200, response.statusCode(), "The response status code should match the expected status code");
        }

    }

}
