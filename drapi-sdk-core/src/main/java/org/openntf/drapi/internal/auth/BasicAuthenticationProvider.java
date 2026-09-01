package org.openntf.drapi.internal.auth;

import static org.openntf.drapi.internal.http.HttpHeaderConstants.APPLICATION_JSON;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.exception.AuthenticationException;
import org.openntf.drapi.exception.DrapiException;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.http.RequestBody;
import org.openntf.drapi.internal.http.ApiPath;
import org.openntf.drapi.internal.log.Log;
import org.openntf.drapi.json.JsonBinding;

public final class BasicAuthenticationProvider extends AuthenticationProviderBase {

    private static final Log LOG = Log.getLogger(BasicAuthenticationProvider.class);

    private static final ApiPath AUTH_PATH = ApiPath.root("/auth");

    public BasicAuthenticationProvider(DrapiConfig config) {
        super(config);
    }

    @Override
    public CompletableFuture<BearerToken> acquireToken(AuthenticationToolkit toolkit) {
        DrapiRequest authRequest = createAuthRequest();

        return toolkit.httpTransport()
                      .submitAsync(authRequest)
                      .thenCompose(authResponse -> {
                          if (authResponse.isSuccess()) {
                              return CompletableFuture.completedFuture(parseBearerTokenFromResponse(authRequest, authResponse));
                          } else if (authResponse.isAuthenticationFailure()) {
                              var exception = new AuthenticationException("Authentication failed", authRequest, authResponse);

                              LOG.debug("Authentication failed for user: " + config().username(), exception);
                              return CompletableFuture.failedFuture(exception);
                          } else {
                              var exception = new DrapiException("Unexpected response from authentication", authRequest, authResponse);

                              LOG.debug("Unexpected response from authentication for user: " + config().username(), exception);
                              return CompletableFuture.failedFuture(exception);
                          }
                      });
    }

    private DrapiRequest createAuthRequest() {
        AuthRequest authRequest = new AuthRequest(config().username(), config().password());
        return DrapiRequest.post(AUTH_PATH)
                           .body(RequestBody.ofString(APPLICATION_JSON, JsonBinding.get().toJson(authRequest)));
    }

    private BearerToken parseBearerTokenFromResponse(DrapiRequest request, DrapiResponse response) {
        var authResponse = JsonBinding.get().fromJson(response.bodyAsString(), AuthResponse.class);

        if (authResponse == null || authResponse.bearer() == null) {
            throw new DrapiException("Invalid authentication response", request, response);
        }

        return new BearerToken(authResponse.bearer(), authResponse.claims());
    }

    @Override
    public boolean supportsRefresh() {
        return true;
    }

    record AuthRequest(String username, String password) {

    }

    record AuthResponse(String bearer, Map<String, Object> claims, int leeway, int expSeconds, String issueDate) {

    }

}
