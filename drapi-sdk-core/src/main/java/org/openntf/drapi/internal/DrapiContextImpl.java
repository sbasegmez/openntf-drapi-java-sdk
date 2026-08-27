package org.openntf.drapi.internal;

import java.util.Objects;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.DrapiContext;
import org.openntf.drapi.internal.auth.AuthenticationProvider;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.json.JsonBinding;

public class DrapiContextImpl implements DrapiContext {

    private final DrapiConfig config;
    private final JsonBinding jsonBinding;
    private final HttpTransport httpTransport;
    private final AuthenticationProvider authenticationProvider;

    DrapiContextImpl(DrapiConfig config, JsonBinding jsonBinding, HttpTransport httpTransport, AuthenticationProvider authenticationProvider) {
        this.config = Objects.requireNonNull(config, "DrapiConfig must not be null");
        this.jsonBinding = Objects.requireNonNull(jsonBinding, "JsonBinding must not be null");
        this.httpTransport = Objects.requireNonNull(httpTransport, "HttpTransport must not be null");
        this.authenticationProvider = Objects.requireNonNull(authenticationProvider, "AuthenticationProvider must not be null");
    }

    public DrapiConfig config() {
        return config;
    }

    public JsonBinding jsonBinding() {
        return jsonBinding;
    }

    public HttpTransport httpTransport() {
        return httpTransport;
    }

    public AuthenticationProvider authenticationProvider() {
        return authenticationProvider;
    }
}
