package org.openntf.drapi.auth.builtin;

import static org.openntf.drapi.internal.http.HttpHeaderConstants.APPLICATION_JSON;

import java.util.concurrent.atomic.AtomicReference;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.auth.SessionContext;
import org.openntf.drapi.auth.Token;
import org.openntf.drapi.auth.TokenSourceBase;
import org.openntf.drapi.exception.AuthenticationException;
import org.openntf.drapi.exception.DrapiException;
import org.openntf.drapi.exception.HttpTransportException;
import org.openntf.drapi.http.ApiPath;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.http.HttpMethod;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.http.RequestBody;
import org.openntf.drapi.internal.dto.AuthRequest;
import org.openntf.drapi.internal.dto.AuthResponse;
import org.openntf.drapi.internal.http.HttpHeaderConstants;
import org.openntf.drapi.internal.log.Log;
import org.openntf.drapi.json.JsonBinding;
import org.openntf.drapi.util.ConfigKey;

public class PasswordTokenSource extends TokenSourceBase {

    private static final Log LOG = Log.getLogger(PasswordTokenSource.class);

    private static final ApiPath AUTH_PATH = ApiPath.root("/auth");
    private static final ApiPath LOGOUT_PATH = AUTH_PATH.append("/logout");

    private static final ConfigKey<String> USERNAME_KEY = ConfigKey.of("auth.username", String.class);
    private static final ConfigKey<String> PASSWORD_KEY = ConfigKey.of("auth.password", String.class);
    private static final ConfigKey<String> SCOPE_KEY = ConfigKey.of("auth.scope", String.class);

    private static final int DEFAULT_SKEW_SECS = 30; // Default skew time in seconds to account for clock differences

    private final String username;
    private final String password;
    private final String scope;

    private final Object lock = new Object();

    // Instead of storing the token in a simple variable, we use an AtomicReference to hold AuthResponse.
    // This allows for thread-safe updates and retrievals of the current authentication response.
    // Also, the Token is a simple element. AuthResponse contains more information, including the expiration time.
    private final AtomicReference<AuthResponse> currentAuthResponse = new AtomicReference<>();

    public PasswordTokenSource(DrapiConfig config, HttpTransport transport) {
        super(config, transport);

        this.username = config().get(USERNAME_KEY)
                                .orElseThrow(() -> new IllegalArgumentException("Username is required for PasswordTokenSource"));
        this.password = config().get(PASSWORD_KEY)
                                .orElseThrow(() -> new IllegalArgumentException("Password is required for PasswordTokenSource"));
        this.scope = config().get(SCOPE_KEY).orElse(null); // Scope is optional
    }

    private AuthResponse popCache() {
        if(currentAuthResponse.get() != null) {
            // If we already have a valid token, return it
            AuthResponse authResponse = currentAuthResponse.get();
            if (authResponse != null && !authResponse.isExpired(DEFAULT_SKEW_SECS)) {
                LOG.trace("Token for user {} is still valid. Returning existing token.", username);
                return authResponse;
            }
        }

        return null;
    }

    @Override
    public Token acquire(SessionContext sessionContext) {
        AuthResponse cachedAuthResponse = popCache();
        if (cachedAuthResponse != null) {
            return cachedAuthResponse.toToken();
        }

        synchronized (lock) {
            // Double-check locking to ensure that we don't acquire a new token if another thread has already done so
            cachedAuthResponse = popCache();
            if (cachedAuthResponse != null) {
                return cachedAuthResponse.toToken();
            }

            // We are now sure that we need to acquire a new token
            DrapiRequest request = createAuthRequest();

            // Currently, the authentication process is synchronous.
            // Later, we may consider making this asynchronous to improve performance and scalability.
            DrapiResponse response = transport().submit(request);

            AuthResponse authResponse = processTokenResponse(request, response);
            currentAuthResponse.set(authResponse);
            return authResponse.toToken();
        }
    }

    @Override
    public void tokenRejected(SessionContext sessionContext, Token token) {
        if (token == null) {
            return;
        }

        AuthResponse current = currentAuthResponse.get();
        if (current != null && current.bearer().equals(token.bearer()) && currentAuthResponse.compareAndSet(current, null)) {
            LOG.debug("Token for user {} was rejected by the server. Invalidating cached token.", username);
        }
    }

    @Override
    public void logout(SessionContext sessionContext) {
        AuthResponse lastCache;

        // In case some other thread was in login process. Slim chance in single-user case.
        synchronized (lock) {
            // Clear the cached token and submit a logout request to the server
            lastCache = currentAuthResponse.getAndSet(null);
        }

        if(lastCache == null) {
            // No token to logout, just return
            return;
        }

        // We don't need to allow a skew in logout.
        if(lastCache.isExpired(0)) {
            // Token is already expired, no need to send logout request
            LOG.debug("Token for user {} is already expired. Clearing cached token.", username);
            return;
        }

        // Let's send a logout request to the server to invalidate the token
        DrapiRequest request = DrapiRequest.create(HttpMethod.GET, LOGOUT_PATH)
                                           .header(HttpHeaderConstants.AUTHORIZATION, "Bearer " + lastCache.bearer(), true);

        // Submit the logout request to the server
        try (DrapiResponse response = transport().submit(request)) {
            if (!response.isSuccess()) {
                LOG.warn("Logout request for user {} failed with status code {}.", username, response.statusCode());
            } else {
                LOG.debug("Successfully logged out user {}.", username);
            }
        } catch (HttpTransportException e) {
            // No need to block the execution, just log the error. The token is already cleared from the cache.
            LOG.warn("Logout request for user {} failed due to transport error", username, e);
        }
    }

    @Override
    public boolean supportsRefresh() {
        return true;
    }

    private DrapiRequest createAuthRequest() {
        AuthRequest authRequest = new AuthRequest(username, password, scope);
        return DrapiRequest.create(HttpMethod.POST, AUTH_PATH)
                           .body(RequestBody.ofString(APPLICATION_JSON, authRequest.toJson()));
    }

    private AuthResponse processTokenResponse(DrapiRequest request, DrapiResponse response) {
        // If successful, parse the bearer token from the response and return it
        if (response.isSuccess()) {
            return parseResponse(request, response);
        }

        if (response.isAuthenticationFailure()) {
            LOG.debug("Authentication failed for user {}", username);
            throw new AuthenticationException("Authentication failed", request, response);
        }

        LOG.debug("Unexpected response from authentication for user {}", username);

        if(LOG.isTraceEnabled()) {
            LOG.trace("Response body: {}", response.bodyAsString().replaceAll("\\n", " "));
        }

        throw new DrapiException("Unexpected response from authentication", request, response);
    }

    private AuthResponse parseResponse(DrapiRequest request, DrapiResponse response) {
        var authResponse = JsonBinding.get().fromJson(response.bodyAsString(), AuthResponse.class);

        if (authResponse == null || authResponse.bearer() == null) {
            throw new DrapiException("Invalid authentication response", request, response);
        }

        LOG.trace("Successfully authenticated user {}. Token expires in {} seconds.", username, authResponse.expSeconds());
        return authResponse;
    }

}
