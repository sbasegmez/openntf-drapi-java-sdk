package org.openntf.drapi.internal.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.exception.AuthenticationException;
import org.openntf.drapi.exception.DrapiException;
import org.openntf.drapi.exception.ErrorMessage;
import org.openntf.drapi.exception.JsonBindingException;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.internal.DrapiConfigBuilder;
import org.openntf.drapi.internal.auth.BasicAuthenticationProvider.AuthResponse;
import org.openntf.drapi.internal.test.AbstractHttpMockTest;
import org.openntf.drapi.json.JsonBinding;
import org.openntf.drapi.json.JsonBindingTestSupport;

@ExtendWith(MockitoExtension.class)
class BasicAuthenticationProviderTest extends AbstractHttpMockTest {

    @Mock
    JsonBinding jsonBinding;

    @BeforeEach
    public void setUp() {
        JsonBindingTestSupport.set(jsonBinding);
    }

    @AfterEach
    public void tearDown() {
        JsonBindingTestSupport.reset();
    }

    @Override
    protected String pathToListen() {
        return "/api/v1/auth";
    }

    @Override
    protected DrapiConfig buildConfig(Consumer<DrapiConfigBuilder> configCustomizer) {
        // Switch to user/password authentication for this test
        return buildConfig(builder -> {
            builder.basic("some-user", "some-password");
            if (configCustomizer != null) {
                configCustomizer.accept(builder);
            }
        }, false);
    }

    protected AuthenticationToolkit toolkit(DrapiConfig config) {
        HttpTransport bareTransport = HttpTransport.defaultTransport(config, null);
        return new AuthenticationToolkit(bareTransport);
    }

    protected BasicAuthenticationProvider provider(DrapiConfig config) {
        return new BasicAuthenticationProvider(config);
    }

    @Test
    @DisplayName("Test BasicAuthenticationProvider with successful response")
    void testBasicAuthenticationProviderWithSuccessfulResponse() {
        DrapiConfig config = buildConfig(null);
        respondWith(200, "Success");

        // Mock the JSON binding to return a specific JSON string when converting the AuthRequest object to JSON
        when(jsonBinding.toJson(any())).thenReturn("{\"username\":\"some-user\",\"password\":\"some-password\"}");

        // Mock the JSON binding to return a specific AuthResponse object if anyone asks for an AuthResponse from a JSON string
        when(jsonBinding.fromJson(anyString(), eq(BasicAuthenticationProvider.AuthResponse.class)))
            .thenReturn(new BasicAuthenticationProvider.AuthResponse("mocked-token", Map.of("exp", 123456789), 0, 3600, null));


        // Let's get the token and assert that it matches the mocked response
        BearerToken token = provider(config).acquireToken(toolkit(config))
                                            .join();

        verify(jsonBinding).toJson(new BasicAuthenticationProvider.AuthRequest("some-user", "some-password"));
        assertEquals("/api/v1/auth", mirrorRequest.get().path(), "The request path should match the expected auth path");
        assertEquals("mocked-token", token.bearer(), "The acquired token should match the mocked token");
        assertEquals(123456789, token.claims().get("exp"), "The token expiration should match the mocked value");
    }

    @Test
    @DisplayName("Test BasicAuthenticationProvider with invalid response (null bearer token)")
    void testBasicAuthenticationProviderWithNullResponse() {
        DrapiConfig config = buildConfig(null);
        respondWith(200, "Success");

        // Mock the JSON binding to return a specific JSON string when converting the AuthRequest object to JSON
        when(jsonBinding.toJson(any())).thenReturn("{\"username\":\"some-user\",\"password\":\"some-password\"}");

        // Mock the JSON binding to return a specific AuthResponse object if anyone asks for an AuthResponse from a JSON string
        when(jsonBinding.fromJson(anyString(), eq(BasicAuthenticationProvider.AuthResponse.class)))
            .thenReturn(new BasicAuthenticationProvider.AuthResponse(null, null, 0, 0, null));

        CompletionException exception = assertThrowsExactly(CompletionException.class, () -> provider(config).acquireToken(toolkit(config)).join());
        assertEquals(DrapiException.class, exception.getCause().getClass(), "The cause of the exception should be an RuntimeException");

        DrapiException drapiException = (DrapiException) exception.getCause();
        assertEquals("Invalid authentication response", drapiException.getMessage(), "The exception message should indicate an invalid authentication response");
    }

    @Test
    @DisplayName("Test BasicAuthenticationProvider with invalid response (Invalid JSON)")
    void testBasicAuthenticationProviderWithInvalidJsonResponse() {
        DrapiConfig config = buildConfig(null);
        respondWith(200, "Success");

        // Mock the JSON binding to return a specific JSON string when converting the AuthRequest object to JSON
        when(jsonBinding.toJson(any())).thenReturn("{\"username\":\"some-user\",\"password\":\"some-password\"}");

        // Mock the JSON binding to return a specific AuthResponse object if anyone asks for an AuthResponse from a JSON string
        when(jsonBinding.fromJson(anyString(), eq(BasicAuthenticationProvider.AuthResponse.class)))
            .thenThrow(new JsonBindingException(("Invalid JSON")));

        CompletionException exception = assertThrowsExactly(CompletionException.class, () -> provider(config).acquireToken(toolkit(config)).join());
        assertEquals(JsonBindingException.class, exception.getCause().getClass(), "The cause of the exception should be a JsonBindingException");
    }

    @Test
    @DisplayName("Test BasicAuthenticationProvider Error 500 with HTML response (Reverse Proxy or Web Server Error)")
    void testBasicAuthenticationProviderWithInvalidHtmlResponse() {
        DrapiConfig config = buildConfig(null);
        respondWith(500, "Server Error");

        // Mock the JSON binding to return a specific JSON string when converting the AuthRequest object to JSON
        when(jsonBinding.toJson(any())).thenReturn("{\"username\":\"some-user\",\"password\":\"some-password\"}");

        // Mock the JSON binding to throw an exception when trying to parse the HTML response into an ErrorMessage object [Unhappy path]
        // We should make sure the Json exception in the ErrorMessage parsing is handled and wrapped in a DrapiException
        when(jsonBinding.fromJson(anyString(), eq(ErrorMessage.class)))
            .thenThrow(new JsonBindingException(("Invalid Json")));

        CompletionException exception = assertThrowsExactly(CompletionException.class, () -> provider(config).acquireToken(toolkit(config)).join());
        assertEquals(DrapiException.class, exception.getCause().getClass(), "The cause of the exception should be a DrapiException");
    }

    @Test
    @DisplayName("Test BasicAuthenticationProvider Error 401 with HTML response (Reverse Proxy or Web Server Error)")
    void testBasicAuthenticationProviderWithUnauthorizedHtmlResponse() {
        DrapiConfig config = buildConfig(null);
        respondWith(401, "Unauthorized");

        // Mock the JSON binding to return a specific JSON string when converting the AuthRequest object to JSON
        when(jsonBinding.toJson(any())).thenReturn("{\"username\":\"some-user\",\"password\":\"some-password\"}");

        // Mock the JSON binding to throw an exception when trying to parse the HTML response into an ErrorMessage object [Unhappy path]
        // We should make sure the Json exception in the ErrorMessage parsing is handled and wrapped in a DrapiException
        when(jsonBinding.fromJson(anyString(), eq(ErrorMessage.class)))
            .thenThrow(new JsonBindingException(("Invalid Json")));

        CompletionException exception = assertThrowsExactly(CompletionException.class, () -> provider(config).acquireToken(toolkit(config)).join());
        assertEquals(AuthenticationException.class, exception.getCause().getClass(), "The cause of the exception should be a AuthenticationException");
    }

    @Test
    @DisplayName("Test BasicAuthenticationProvider with wrong username/password")
    void testBasicAuthenticationProviderWithWrongUsernamePassword() {
        DrapiConfig config = buildConfig(null);
        respondWith(401, "{\"status\": 401,\"message\": \"Invalid credentials or account locked\"}");

        // Mock the JSON binding to return a specific JSON string when converting the AuthRequest object to JSON
        when(jsonBinding.toJson(any())).thenReturn("{\"username\":\"some-user\",\"password\":\"some-password\"}");

        when(jsonBinding.fromJson(anyString(), eq(ErrorMessage.class)))
            .thenReturn(new ErrorMessage(401, "Invalid credentials or account locked", null, 900));

        CompletionException exception = assertThrowsExactly(CompletionException.class, () -> provider(config).acquireToken(toolkit(config)).join());
        assertEquals(AuthenticationException.class, exception.getCause().getClass(), "The cause of the exception should be an AuthenticationException");

        AuthenticationException authException = (AuthenticationException) exception.getCause();
        assertEquals("Authentication failed [Invalid credentials or account locked]", authException.getMessage(), "The exception message should indicate authentication failure");
        assertEquals(401, authException.getStatusCode(), "The response status code should be 401 for authentication failure");
    }

}
