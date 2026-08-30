package org.openntf.drapi.internal.auth;

import java.util.Map;
import org.openntf.drapi.DrapiConfig;

public final class TokenAuthenticationProvider extends AuthenticationProviderBase {

    public TokenAuthenticationProvider(DrapiConfig config) {
        super(config);
    }

    /**
     * Acquires a token for the given context.
     *
     * @param toolkit the toolkit providing access to the context and configuration
     * @return the token, never {@code null}
     * @throws org.openntf.drapi.exception.AuthenticationException if a token could not be obtained
     */
    @Override
    public BearerToken acquireToken(AuthenticationToolkit toolkit) {
        // Token authentication provider simply returns the token from the config, as it is assumed to be a fixed, externally-obtained token.
        return new BearerToken(config.token(), Map.of());
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
        // Token authentication provider does not support refresh, as it is assumed to be a fixed, externally-obtained token.
        return false;
    }

}
