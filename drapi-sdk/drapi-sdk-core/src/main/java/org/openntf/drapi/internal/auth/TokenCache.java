package org.openntf.drapi.internal.auth;

import java.util.Objects;
import java.util.function.Supplier;

public final class TokenCache {

    // The cached token. This is the token that will be returned by the get() method if it is still valid.
    // If the token is null or expired, a new token will be acquired using the tokenSupplier.
    private BearerToken token = null;

    // Supplier to acquire a new token when needed. This allows for lazy acquisition of tokens and avoids unnecessary token requests.
    private final Supplier<BearerToken> tokenSupplier;

    /**
     * Creates a new TokenCache with the given token supplier. The token supplier is used to acquire a new token when the cached token
     * is null or expired.
     * <p>
     * Token Supplier should be thread-safe and handle token acquisition correctly to avoid race conditions in multi-threaded
     * environments. Also MUST NOT produce a null result.
     *
     * @param tokenSupplier the supplier to acquire a new token
     */
    public TokenCache(Supplier<BearerToken> tokenSupplier) {
        this.tokenSupplier = Objects.requireNonNull(tokenSupplier);
    }

    /**
     * Returns the cached token if it is still valid. If the cached token is null or expired, a new token will be acquired using the
     * tokenSupplier. This method is synchronized to ensure thread safety when accessing the cached token.
     *
     * @return the cached token if it is still valid, or a new token acquired from the tokenSupplier
     */
    public synchronized BearerToken get() {
        if (token == null || token.isExpired()) {
            token = tokenSupplier.get();

            if(token == null) {
                throw new IllegalStateException("Token supplier returned null. Token supplier must not return null.");
            }
        }

        return token;
    }

    /**
     * Invalidates the cached token if it matches the given rejected token. This method is synchronized to ensure thread safety when
     * accessing the cached token. If the cached token is invalidated, it will be set to null, and the next call to get() will acquire a new token using the tokenSupplier.
     *
     * @param rejectedToken the token to be invalidated
     * @return true if the cached token was invalidated, false otherwise
     */
    public synchronized boolean invalidate(BearerToken rejectedToken) {
        // Use "==" to compare the token references, as we want to invalidate the cached token only if it is the same instance as the rejected token.
        if (token == rejectedToken) {
            token = null;
            return true;
        }
        return false;
    }


}
