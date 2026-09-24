package org.openntf.drapi.auth;

import org.openntf.drapi.exception.AuthenticationException;
import org.openntf.drapi.exception.DrapiException;
import org.openntf.drapi.exception.HttpTransportException;

/**
 * Interface for authentication mechanism providing Token for the SDK.
 */
public sealed interface TokenSource permits TokenSourceBase {

    /**
     * Acquires a token for the given session context.
     * <p>
     * This is typically used to obtain a token that can be used for making authenticated requests to a service or API. The
     * implementation of this method should handle the necessary logic to retrieve or generate a valid token based on the
     * provided session context.
     * <p>
     * The method is synchronous and should return a valid Token object. It might block but should be short. It's recommended to
     * implement caching or token reuse strategies to minimize the overhead of acquiring tokens frequently.
     * <p>
     * The implementation should not rely on ThreadLocal, as the caller thread can change between calls. It should also be thread-safe,
     * as multiple threads may call this method for the same session concurrently.
     *
     * @param sessionContext sessionContext object containing information about the current session
     * @return a Token representing the acquired access token
     * @throws HttpTransportException if there is a transport error while acquiring the token
     * @throws AuthenticationException if the authentication fails for any reason
     * @throws DrapiException for any other unexpected issues that may occur during token acquisition
     */
    Token acquire(SessionContext sessionContext);

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
     * @param token          the Token that was rejected by the server
     */
    void tokenRejected(SessionContext sessionContext, Token token);

    /**
     * Logs out the user associated with the given session context. This method is called when the user explicitly logs out of the
     * session. The implementation may or may not invalidate the token, depending on the specific requirements of the implementation. If
     * the implementation chooses to invalidate the token, it should ensure that any cached tokens are also invalidated to prevent
     * further use of the token after logout.
     *
     * @param sessionContext sessionContext object containing information about the current session
     */
    void logout(SessionContext sessionContext);

    /**
     * Indicates whether the implementation supports token refresh. If this method returns true, the SDK will attempt to refresh the
     * token when it is rejected by the server. If it returns false, the SDK will not attempt to refresh the token and will instead
     * treat the rejection as a permanent failure.
     *
     * @return true if the implementation supports token refresh, false otherwise
     */
    boolean supportsRefresh();

}
