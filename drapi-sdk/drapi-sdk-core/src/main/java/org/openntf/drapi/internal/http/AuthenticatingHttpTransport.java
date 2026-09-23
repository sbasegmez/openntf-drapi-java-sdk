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
import org.openntf.drapi.auth.Token;
import org.openntf.drapi.auth.TokenSource;
import org.openntf.drapi.auth.SessionContext;
import org.openntf.drapi.exception.AuthenticationException;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.internal.log.Log;

/**
 * A wrapper around an HttpTransport that adds authentication capabilities. This class is intended to be used internally by the SDK to
 * handle authentication when making HTTP requests.
 * <p>
 * Delegate pattern is used to prevent circular dependencies between the HttpTransport and TokenSource. The AuthenticatingHttpTransport
 * will delegate the actual HTTP request to the underlying HttpTransport, while also managing authentication tokens and refreshing them
 * as needed.
 */
public final class AuthenticatingHttpTransport implements HttpTransport {

    private static final Log LOG = Log.getLogger(AuthenticatingHttpTransport.class);

    private final HttpTransport httpTransport;
    private final TokenSource tokenSource;

    public AuthenticatingHttpTransport(HttpTransport httpTransport, TokenSource tokenSource) {
        this.httpTransport = Objects.requireNonNull(httpTransport);
        this.tokenSource = Objects.requireNonNull(tokenSource);
    }

    /**
     * This is the functional reference to the token acquisition method. It is used to acquire a new token when the current token is
     * invalid or expired. The given request MUST HAVE a sessionContext set, otherwise the token acquisition will fail. The
     * sessionContext is used to provide context for the token acquisition process, such as the current user session or other relevant
     * information.
     *
     * @param request the DrapiRequest for which the token is being acquired
     * @return a new Token
     */
    private Token acquireToken(DrapiRequest request) {
        SessionContext sessionContext = request.sessionContext().orElseThrow();

        LOG.trace("Acquiring new token using {}", tokenSource.getClass().getSimpleName());

        try {
            Token token = tokenSource.acquire(sessionContext);

            if (token == null) {
                throw new AuthenticationException("Unable to receive a token", request, null);
            }

            return token;
        } catch (Exception e) {
            // TODO : In case of Oauth, the exception will determine if the app should require a new login.
            //  SDK cannot initiate an OAuth dance, but we can signal to the app-developer that a new login is required.
            //  This can be done by a specific exception (e.g. AuthenticationRequiredException). Revisit with Oauth implementation.
            LOG.error("Failed to acquire token for {}", sessionContext.username(), e);
            throw new AuthenticationException("Failed to acquire token", request, null, e);
        }
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
        // Validate the request to ensure it is not null and it has a sessionContext set.
        Objects.requireNonNull(request, "request must not be null");
        request.sessionContext().orElseThrow(() -> new IllegalArgumentException("request must have a sessionContext set"));

        return submitRequestWithToken(request, true);
    }

    @Override
    public void stop() {
        httpTransport.stop();
    }

    /**
     * We need this method with retryIfNeeded parameter to avoid infinite loops in case of repeated authentication failures.
     *
     * @param request       the DrapiRequest to submit
     * @param retryIfNeeded flag indicating whether to retry the request if an authentication failure occurs
     * @return a CompletableFuture that will be completed with the DrapiResponse
     */
    private CompletableFuture<DrapiResponse> submitRequestWithToken(DrapiRequest request, boolean retryIfNeeded) {
        LOG.trace("Submitting {} request to {} with retryIfNeeded={}", request.httpMethod(), request.path(), retryIfNeeded);

        // Get a token. If there is no valid token, this will result in AuthenticationException.
        try {
            Token token = acquireToken(request);

            // Insert the token into the request headers
            request.header(HttpHeaderConstants.AUTHORIZATION, "Bearer " + token.bearer(), true);

            return httpTransport.submitAsync(request)
                                .thenCompose(response -> peekResponse(request, response, retryIfNeeded, token));
        } catch (AuthenticationException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // This method checks the response for authentication failures. If the response indicates an authentication failure,
    // and we are allowed to retry, it will attempt to refresh the token and retry the request.
    private CompletableFuture<DrapiResponse> peekResponse(DrapiRequest request, DrapiResponse response, boolean retryIfNeeded, Token currentToken) {
        // We are pretty sure that the request has a session context, because we only call this method after acquiring a token for a session context.
        SessionContext sessionContext = request.sessionContext().orElseThrow();

        if (response.isAuthenticationFailure() && retryIfNeeded && tokenSource.supportsRefresh()) {

            LOG.trace("We will retry {} request for {}", request.httpMethod(), request.path());

            // If the token source still has the same token, we can invalidate it to force a refresh.
            // If another thread has already refreshed the token, we will not invalidate it.
            tokenSource.tokenRejected(sessionContext, currentToken);

            // Now we can attempt to submit again. But this time if it fails, we will not retry again to avoid infinite loops.
            return submitRequestWithToken(request, false);
        }

        if(! response.isSuccess()) {
            // If the response is not successful, we log it for debugging purposes.
            LOG.trace("No retry needed for {} request to {}", request.httpMethod(), request.path());
        }

        // The response is acceptable, so we can return it
        return CompletableFuture.completedFuture(response);
    }

}
