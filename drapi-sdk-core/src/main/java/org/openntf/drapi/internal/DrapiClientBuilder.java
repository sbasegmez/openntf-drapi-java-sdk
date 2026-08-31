package org.openntf.drapi.internal;

import java.util.Objects;
import java.util.concurrent.Executor;
import org.openntf.drapi.DrapiClient;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.DrapiContext;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.internal.auth.AuthenticationProvider;

public class DrapiClientBuilder {
    final DrapiConfig config;

    // Temporary fields to hold the provided components before building the DrapiContext
    private HttpTransport httpTransport;
    private Executor httpExecutor;
    private AuthenticationProvider authenticationProvider;

    public DrapiClientBuilder(DrapiConfig config) {
        this.config = Objects.requireNonNull(config, "Config must not be null");
    }

    public DrapiClientBuilder httpTransport(HttpTransport httpTransport) {
        this.httpTransport = httpTransport;
        return this;
    }

    public DrapiClientBuilder httpExecutor(Executor httpExecutor) {
        this.httpExecutor = httpExecutor;
        return this;
    }

    public DrapiClient build() {

        if (httpTransport == null) {
            // Fallback to default HTTP transport if not provided
            // If HttpExecutor is not provided, it will be handled inside the relevant constructor
            httpTransport = HttpTransport.defaultTransport(config, httpExecutor);
        }

        if (authenticationProvider == null) {
            // Fallback to default authentication provider if not provided
            authenticationProvider = AuthenticationProvider.create(config);
        }

        DrapiContext context = new DrapiContextImpl(config, httpTransport, authenticationProvider);
        return new DrapiClientImpl(config, context);
    }

}
