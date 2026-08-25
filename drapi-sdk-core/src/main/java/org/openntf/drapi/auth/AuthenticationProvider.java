package org.openntf.drapi.auth;

import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.internal.auth.BasicAuthenticationProvider;
import org.openntf.drapi.internal.auth.OAuthAuthenticationProvider;
import org.openntf.drapi.internal.auth.TokenAuthenticationProvider;

public interface AuthenticationProvider {

    /**
     * Acquires a token for the given context.
     *
     * @param context the client's base URL, HTTP client and codec
     * @return the token, never {@code null}
     * @throws org.openntf.drapi.exception.AuthenticationException if a token could not be obtained
     */
    JwtToken acquireToken(Object context);

    /**
     * Reports whether asking again after a 401 is worthwhile.
     * <p>
     * Return {@code false} for a provider handed a fixed, externally-obtained token: re-asking would return the same
     * rejected value, and failing immediately gives the caller a clearer error than a silent retry.
     *
     * @return whether the SDK should re-acquire after a 401, {@code true} by default
     */
    boolean supportsRefresh();

    static AuthenticationProvider get(DrapiConfig config) {
        return switch (config.authType()) {
            case BASIC -> new BasicAuthenticationProvider(config);
            case TOKEN -> new TokenAuthenticationProvider(config);
            case OAUTH -> new OAuthAuthenticationProvider(config);
        };
    }

}
