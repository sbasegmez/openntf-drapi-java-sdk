package org.openntf.drapi.internal.auth;

import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.exception.AuthenticationException;

public final class OAuthAuthenticationProvider extends AuthenticationProviderBase {

    public OAuthAuthenticationProvider(DrapiConfig config) {
        super(config);
        throw new UnsupportedOperationException("OAuth authentication is not yet implemented.");
    }

    /**
     * Acquires a token for the given context.
     *
     * @param toolkit the toolkit providing context for token acquisition
     * @return the token, never {@code null}
     * @throws AuthenticationException if a token could not be obtained
     */
    @Override
    public BearerToken acquireToken(AuthenticationToolkit toolkit) {
        return null;
    }

    /**
     * Reports whether asking again after a 401 is worthwhile.
     * <p>
     * Return {@code false} for a provider handed a fixed, externally-obtained token: re-asking would return the same rejected value,
     * and failing immediately gives the caller a clearer error than a silent retry.
     *
     * @return whether the SDK should re-acquire after a 401, {@code true} by default
     */
    @Override
    public boolean supportsRefresh() {
        return false;
    }
}
