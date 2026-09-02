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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.internal.auth.AuthenticationProvider;
import org.openntf.drapi.internal.auth.AuthenticationToolkit;
import org.openntf.drapi.internal.auth.BearerToken;
import org.openntf.drapi.internal.auth.TokenCache;
import org.openntf.drapi.internal.log.Log;

/**
 * A wrapper around an HttpTransport that adds authentication capabilities. This class is intended to be used internally by the SDK to
 * handle authentication when making HTTP requests.
 * <p>
 * This class is also responsible for managing the authentication tokens, including acquiring and refreshing them as needed. It uses an
 * AuthenticationProvider interface to acquire tokens.
 * <p>
 * Authentication token management is susceptible to racing conditions, especially in non-blocking or multi-threaded environments.
 * Therefore, it is crucial to ensure that the AuthenticationProvider implementation is thread-safe and handles token acquisition and
 * refresh correctly. This class specifically uses non-blocking concurrency tricks to prevent race conditions while keeping the
 * non-blocking nature of the HTTP transport intact.
 *
 * <p>
 * Delegate pattern is used to prevent circular dependencies between the HttpTransport and AuthenticationProvider. The
 * AuthenticatingHttpTransport will delegate the actual HTTP request to the underlying HttpTransport, while also managing authentication
 * tokens and refreshing them as needed.
 */
public class AuthenticatingHttpTransport implements HttpTransport {

    private static final Log LOG = Log.getLogger(AuthenticatingHttpTransport.class);

    private final TokenCache tokenCache;

    private final AuthenticationToolkit authenticationToolkit;
    private final AuthenticationProvider authenticationProvider;

    public AuthenticatingHttpTransport(AuthenticationToolkit authenticationToolkit, AuthenticationProvider authenticationProvider) {
        this.tokenCache = new TokenCache(this::acquireToken);
        this.authenticationToolkit = Objects.requireNonNull(authenticationToolkit);
        this.authenticationProvider = Objects.requireNonNull(authenticationProvider);
    }

    /**
     * This is the functional reference to the TokenCache's token acquisition method. It will be called by the TokenCache when it needs
     * to acquire a new token.
     *
     * @return a new BearerToken
     */
    private BearerToken acquireToken() {
        LOG.trace("Acquiring new token using {}", authenticationProvider.getClass().getSimpleName());
        return authenticationProvider.acquireToken(authenticationToolkit);
    }


    /**
     * Submits a DrapiRequest asynchronously. Implementations of this method should handle the request submission and return a
     * CompletableFuture that will be completed with the DrapiResponse when the request is processed.
     *
     * @param request the DrapiRequest to submit
     * @return a CompletableFuture that will be completed with the DrapiResponse
     */
    @Override
    public CompletableFuture<DrapiResponse> submitAsync(DrapiRequest request) {
        return submitRequestWithToken(request, true);
    }

    // We need this method with retryIfNeeded parameter to avoid infinite loops in case of repeated authentication failures.
    private CompletableFuture<DrapiResponse> submitRequestWithToken(DrapiRequest request, boolean retryIfNeeded) {
        LOG.trace("Submitting {} request to {} with retryIfNeeded={}", request.httpMethod(), request.path(), retryIfNeeded);

        // Get the current token promise. If there is no valid token, this will trigger a token acquisition process.
        BearerToken token = tokenCache.get();

        return injectTokenAndSend(request, token)
            .thenCompose(response -> peekResponse(request, response, retryIfNeeded, token));
    }

    // Inject Token into the request and send it using the delegate HttpTransport.
    private CompletableFuture<DrapiResponse> injectTokenAndSend(DrapiRequest request, BearerToken token) {
        // We have a valid token, so we can proceed with the request
        request.header(HttpHeaderConstants.AUTHORIZATION, "Bearer " + token.bearer());

        return authenticationToolkit.httpTransport()
                                    .submitAsync(request);
    }

    // This method checks the response for authentication failures. If the response indicates an authentication failure,
    // and we are allowed to retry, it will attempt to refresh the token and retry the request.
    private CompletableFuture<DrapiResponse> peekResponse(DrapiRequest request, DrapiResponse response, boolean retryIfNeeded, BearerToken currentToken) {
        if (response.isAuthenticationFailure() && retryIfNeeded && authenticationProvider.supportsRefresh()) {

            LOG.trace("We will retry {} request for {}", request.httpMethod(), request.path());

            // If the tokenCache still has the same token, we can invalidate it to force a refresh.
            // If another thread has already refreshed the token, we will not invalidate it.
            tokenCache.invalidate(currentToken);

            // Now we can attempt to submit again. But this time if it fails, we will not retry again to avoid infinite loops.
            return submitRequestWithToken(request, false);
        }

        LOG.trace("No retry needed for {} request to {}", request.httpMethod(), request.path());

        // The response is acceptable, so we can return it
        return CompletableFuture.completedFuture(response);
    }

}
