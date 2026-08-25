package org.openntf.drapi.internal.auth;

import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.auth.AuthenticationProvider;

public abstract class AbstractAuthenticationProvider implements AuthenticationProvider {

    protected final DrapiConfig config;

    protected AbstractAuthenticationProvider(DrapiConfig config) {
        this.config = config;
    }

    protected DrapiConfig config() {
        return this.config;
    }
}
