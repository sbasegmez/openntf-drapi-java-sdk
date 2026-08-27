package org.openntf.drapi.internal.auth;

import org.openntf.drapi.DrapiConfig;

public sealed interface AuthenticationProvider permits AuthenticationProviderBase {

    /**
     * Acquires a token for the given context.
     *
     * @param context the client's base URL, HTTP client and codec
     * @return the token, never {@code null}
     * @throws org.openntf.drapi.exception.AuthenticationException if a token could not be obtained
     */
    BearerToken acquireToken(Object context);

    /**
     * Reports whether asking again after a 401 is worthwhile.
     * <p>
     * Return {@code false} for a provider handed a fixed, externally-obtained token: re-asking would return the same
     * rejected value, and failing immediately gives the caller a clearer error than a silent retry.
     *
     * @return whether the SDK should re-acquire after a 401, {@code true} by default
     */
    boolean supportsRefresh();

    static AuthenticationProvider create(DrapiConfig config) {
        return switch (config.authType()) {
            case BASIC -> new BasicAuthenticationProvider(config);
            case TOKEN -> new TokenAuthenticationProvider(config);
            case OAUTH -> new OAuthAuthenticationProvider(config);
        };
    }

}
