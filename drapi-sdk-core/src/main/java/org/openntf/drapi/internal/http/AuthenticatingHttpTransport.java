package org.openntf.drapi.internal.http;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.internal.auth.AuthenticationProvider;
import org.openntf.drapi.internal.auth.AuthenticationToolkit;
import org.openntf.drapi.internal.auth.BearerToken;

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

    private final AuthenticationToolkit authenticationToolkit;
    private final HttpTransport delegate;
    private final AuthenticationProvider authenticationProvider;

    // We keep track of the current token acquisition process using an AtomicReference to a CompletableFuture.
    // This allows us to handle concurrent requests that may require a token refresh without blocking the main thread.
    // JS developers in the room will smile at this...
    private final AtomicReference<CompletableFuture<BearerToken>> atomicTokenPromise = new AtomicReference<>();

    public AuthenticatingHttpTransport(AuthenticationToolkit authenticationToolkit, AuthenticationProvider authenticationProvider) {
        this.authenticationToolkit = Objects.requireNonNull(authenticationToolkit);
        this.delegate = authenticationToolkit.httpTransport();
        this.authenticationProvider = Objects.requireNonNull(authenticationProvider);
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
        // Get the current token promise. If there is no valid token, this will trigger a token acquisition process.
        var currentPromise = getTokenPromise();

        return currentPromise.thenCompose(token -> injectToken(request, token))
                             .thenCompose(response -> peekResponse(request, response, retryIfNeeded, currentPromise));
    }

    // This method checks the response for authentication failures. If the response indicates an authentication failure,
    // and we are allowed to retry, it will attempt to refresh the token and retry the request.
    private CompletableFuture<DrapiResponse> injectToken(DrapiRequest request, BearerToken token) {
        // We have a valid token, so we can proceed with the request
        request.header(HttpHeaderConstants.AUTHORIZATION, "Bearer " + token.bearer());
        return delegate.submitAsync(request);
    }

    private CompletableFuture<DrapiResponse> peekResponse(DrapiRequest request, DrapiResponse response, boolean retryIfNeeded, CompletableFuture<BearerToken> currentPromise) {
        if (response.isAuthenticationFailure() && retryIfNeeded && authenticationProvider.supportsRefresh()) {
            // The response indicates an authentication failure, and we are allowed to retry.
            // We will attempt to refresh the token and retry the request.
            // But we need to invalidate current token promise first, so that the next call to
            // getTokenPromise() will trigger a new token acquisition process.
            // Meanwhile, some other thread may have already started a token refresh process.
            atomicTokenPromise.compareAndSet(currentPromise, null);

            // Now we can attempt to submit again. But this time if it fails, we will not retry again to avoid infinite loops.
            return submitRequestWithToken(request, false);
        }

        // The response is valid, so we can return it
        return CompletableFuture.completedFuture(response);
    }

    private CompletableFuture<BearerToken> getTokenPromise() {
        CompletableFuture<BearerToken> currentPromise = atomicTokenPromise.get();

        if (currentPromise == null || currentPromise.isCompletedExceptionally()) {
            // Nobody tried to acquire a token yet, or the previous attempt failed, so we create a new promise and try to set it atomically
            CompletableFuture<BearerToken> newPromise = authenticationProvider.acquireToken(authenticationToolkit);

            if (atomicTokenPromise.compareAndSet(currentPromise, newPromise)) {
                // We successfully set the new promise, so we return it
                return newPromise;
            } else {
                // The atomic reference was updated by another thread before we could set our new promise.
                // So we discard our new promise and return the other thread's promise instead
                return atomicTokenPromise.get();
            }
        }

        // If we reach here, it means that there is already a promise and it's not completed exceptionally.
        return currentPromise;
    }

}
