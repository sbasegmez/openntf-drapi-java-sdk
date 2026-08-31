package org.openntf.drapi.internal;

import java.util.Objects;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.DrapiContext;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.internal.auth.AuthenticationProvider;

public class DrapiContextImpl implements DrapiContext {

    private final DrapiConfig config;
    private final HttpTransport httpTransport;
    private final AuthenticationProvider authenticationProvider;

    DrapiContextImpl(DrapiConfig config, HttpTransport httpTransport, AuthenticationProvider authenticationProvider) {
        this.config = Objects.requireNonNull(config, "DrapiConfig must not be null");
        this.httpTransport = Objects.requireNonNull(httpTransport, "HttpTransport must not be null");
        this.authenticationProvider = Objects.requireNonNull(authenticationProvider, "AuthenticationProvider must not be null");
    }

    public DrapiConfig config() {
        return config;
    }

    public HttpTransport httpTransport() {
        return httpTransport;
    }

    public AuthenticationProvider authenticationProvider() {
        return authenticationProvider;
    }
}
