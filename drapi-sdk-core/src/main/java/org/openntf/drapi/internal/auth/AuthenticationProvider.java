package org.openntf.drapi.internal.auth;

import org.openntf.drapi.DrapiConfig;

public sealed interface AuthenticationProvider permits AuthenticationProviderBase {

    /**
     * Acquires a token for the given context.
     *
     * @throws org.openntf.drapi.exception.AuthenticationException if a token could not be obtained
     */
    BearerToken acquireToken(AuthenticationToolkit toolkit);

    /**
     * Reports whether asking again after a 401 is worthwhile.
     * <p>
     * Return {@code false} for a provider handed a fixed, externally-obtained token: re-asking would return the same rejected value,
     * and failing immediately gives the caller a clearer error than a silent retry.
     *
     * @return whether the SDK should re-acquire after a 401, {@code true} by default
     */
    boolean supportsRefresh();

    /**
     * Factory method to create an appropriate AuthenticationProvider based on the DrapiConfig.
     *
     * @param config the DrapiConfig containing authentication type and related settings
     * @return an instance of AuthenticationProvider suitable for the specified authentication type
     */
    static AuthenticationProvider create(DrapiConfig config) {
        return switch (config.authType()) {
            case BASIC -> new BasicAuthenticationProvider(config);
            case TOKEN -> new TokenAuthenticationProvider(config);
            case OAUTH -> new OAuthAuthenticationProvider(config);
        };
    }

}
