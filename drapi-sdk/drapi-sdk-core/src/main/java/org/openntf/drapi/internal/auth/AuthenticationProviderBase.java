package org.openntf.drapi.internal.auth;

import org.openntf.drapi.DrapiConfig;

public abstract sealed class AuthenticationProviderBase implements AuthenticationProvider
    permits BasicAuthenticationProvider, TokenAuthenticationProvider, OAuthAuthenticationProvider {

    protected final DrapiConfig config;

    protected AuthenticationProviderBase(DrapiConfig config) {
        this.config = config;
    }

    protected DrapiConfig config() {
        return this.config;
    }
}
