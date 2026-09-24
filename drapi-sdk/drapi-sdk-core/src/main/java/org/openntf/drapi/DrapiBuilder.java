/*
 * Copyright (c) 2026 Serdar Basegmez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.drapi;

import java.util.Objects;
import java.util.concurrent.Executor;
import org.openntf.drapi.auth.TokenSource;
import org.openntf.drapi.auth.TokenSourceProvider;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.http.HttpTransportProvider;
import org.openntf.drapi.internal.DrapiImpl;

/**
 * Builder class for creating instances of {@link Drapi}.
 * <p>
 * This builder allows you to configure the necessary components for creating a Drapi instance, including the configuration, token
 * source provider, HTTP transport provider, and executor.
 */
public class DrapiBuilder {

    private final DrapiConfig config;
    private final TokenSourceProvider tokenSourceProvider;

    // Temporary fields to hold the provided components before building the DrapiContext
    private HttpTransportProvider httpTransportProvider;
    private Executor httpExecutor;

    /**
     * Constructs a new DrapiBuilder with the specified configuration and token source provider.
     *
     * @param config              The Drapi configuration.
     * @param tokenSourceProvider The token source provider for authentication.
     */
    public DrapiBuilder(DrapiConfig config, TokenSourceProvider tokenSourceProvider) {
        this.config = Objects.requireNonNull(config, "Config must not be null");
        this.tokenSourceProvider = Objects.requireNonNull(tokenSourceProvider, "TokenSourceProvider must not be null");
    }

    /**
     * Sets the HTTP transport provider for the Drapi instance.
     * <p>
     * If not set, a default HTTP transport provider will be used.
     *
     * @param httpTransportProvider The HTTP transport provider.
     * @return The current DrapiBuilder instance.
     */
    public DrapiBuilder httpTransportProvider(HttpTransportProvider httpTransportProvider) {
        this.httpTransportProvider = httpTransportProvider;
        return this;
    }

    /**
     * Sets the HTTP executor for the Drapi instance.
     * <p>
     * If not set, HTTP Transport will use its default executor.
     *
     * @param httpExecutor The HTTP executor.
     * @return The current DrapiBuilder instance.
     */
    public DrapiBuilder httpExecutor(Executor httpExecutor) {
        this.httpExecutor = httpExecutor;
        return this;
    }

    /**
     * Builds a new instance of {@link Drapi} with the configured components.
     *
     * @return a new Drapi instance
     */
    public Drapi build() {

        if (httpTransportProvider == null) {
            // Fallback to default HTTP transport provider if not provided
            // If HttpExecutor is not provided, it will be handled inside the relevant constructor
            this.httpTransportProvider = HttpTransportProvider.defaultTransportProvider();
        }

        // This will create the HttpTransport and TokenSource using the provided or default components
        HttpTransport httpTransport = httpTransportProvider.create(config, httpExecutor);
        TokenSource tokenSource = tokenSourceProvider.create(config, httpTransport);

        // DrapiImpl will internally convert httpTransport and tokenSource to AuthenticatedHttpTransport
        return new DrapiImpl(config, httpTransport, tokenSource);
    }

}
