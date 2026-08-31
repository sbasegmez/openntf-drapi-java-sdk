package org.openntf.drapi.internal.auth;

import static org.openntf.drapi.internal.http.HttpHeaderConstants.APPLICATION_JSON;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.exception.AuthenticationException;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.http.RequestBody;
import org.openntf.drapi.internal.http.ApiPath;
import org.openntf.drapi.json.JsonBinding;

public final class BasicAuthenticationProvider extends AuthenticationProviderBase {

    private static final ApiPath AUTH_PATH = ApiPath.root("/auth");

    public BasicAuthenticationProvider(DrapiConfig config) {
        super(config);
    }

    @Override
    public CompletableFuture<BearerToken> acquireToken(AuthenticationToolkit toolkit) {
        DrapiRequest request = DrapiRequest.post(AUTH_PATH)
                                           .body(RequestBody.ofString(APPLICATION_JSON, jsonBodyForBasicAuth()));

        return toolkit.httpTransport().submitAsync(request)
                      .thenCompose(response -> {
                          if (response.isSuccess()) {
                              return CompletableFuture.completedFuture(parseBearerTokenFromResponse(response));
                          } else if (response.isAuthenticationFailure()) {
                                // TODO Handle authentication failure more gracefully, possibly by throwing a custom exception
                                return CompletableFuture.failedFuture(new AuthenticationException(
                                    "Authentication failed: " + response.statusCode()));
                          } else {
                              // TODO Handle unexpected response more gracefully, possibly by throwing a custom exception
                              return CompletableFuture.failedFuture(new RuntimeException(
                                  "Unexpected response: " + response.statusCode()));
                          }
                      });
    }

    private BearerToken parseBearerTokenFromResponse(DrapiResponse response) {
        var authResponse = JsonBinding.get().fromJson(response.bodyAsString(), AuthResponse.class);

        if(authResponse == null || authResponse.bearer() == null) {
            // TODO Handle error response more gracefully, possibly by throwing a custom exception
            throw new RuntimeException("Invalid authentication response: " + response.bodyAsString());
        }

        return new BearerToken(authResponse.bearer(), authResponse.claims());
    }

    // Construct the JSON body for basic authentication
    private String jsonBodyForBasicAuth() {
        return JsonBinding.get().toJson(new AuthRequest(config().username(), config().password()));
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
