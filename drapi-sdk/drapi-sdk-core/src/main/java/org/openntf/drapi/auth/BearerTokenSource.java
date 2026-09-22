package org.openntf.drapi.auth;

/**
 * Interface for authentication mechanism providing Bearer token for the SDK.
 */
public interface BearerTokenSource {

    /**
     * Acquires a bearer token for the given session context.
     * <p>
     * This is typically used to obtain a token that can be used for making authenticated requests to a service or API. The
     * implementation of this method should handle the necessary logic to retrieve or generate a valid bearer token based on the
     * provided session context.
     * <p>
     * The method is synchronous and should return a valid BearerToken object. It might block but should be short. It's recommended to
     * implement caching or token reuse strategies to minimize the overhead of acquiring tokens frequently.
     * <p>
     * The implementation should not rely on ThreadLocal, as the caller thread can change between calls. It should also be thread-safe,
     * as multiple threads may call this method for the same session concurrently.
     *
     * @param sessionContext sessionContext object containing information about the current session
     * @return a BearerToken representing the acquired access token
     */
    BearerToken acquire(SessionContext sessionContext);

    /**
     * If provided token is rejected by the server, this method will be called to notify the implementation that (probably-cached) token
     * is no longer valid. The token should be invalidated. However, in case multiple threads are racing for the same token, the SDK
     * will provide the invalidated token to the implementation. Implementations should match the cached token before invalidating it,
     * to avoid invalidating a token that is still valid.
     * <p>
     * The implementation should not rely on ThreadLocal, as the caller thread can change between calls. It should also be thread-safe,
     * as multiple threads may call this method for the same session concurrently.
     *
     * @param sessionContext sessionContext object containing information about the current session
     * @param token          the BearerToken that was rejected by the server
     */
    void tokenRejected(SessionContext sessionContext, BearerToken token);

    /**
     * Logs out the user associated with the given session context. This method is called when the user explicitly logs out of the
     * session. The implementation may or may not invalidate the token, depending on the specific requirements of the implementation. If
     * the implementation chooses to invalidate the token, it should ensure that any cached tokens are also invalidated to prevent
     * further use of the token after logout.
     *
     * @param sessionContext sessionContext object containing information about the current session
     */
    void logout(SessionContext sessionContext);

}
