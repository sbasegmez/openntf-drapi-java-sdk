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

public class DrapiBuilder {
    private final DrapiConfig config;
    private final TokenSourceProvider tokenSourceProvider;

    // Temporary fields to hold the provided components before building the DrapiContext
    private HttpTransportProvider httpTransportProvider;
    private Executor httpExecutor;

    public DrapiBuilder(DrapiConfig config, TokenSourceProvider tokenSourceProvider) {
        this.config = Objects.requireNonNull(config, "Config must not be null");
        this.tokenSourceProvider = Objects.requireNonNull(tokenSourceProvider, "TokenSourceProvider must not be null");
    }

    public DrapiBuilder httpTransportProvider(HttpTransportProvider httpTransportProvider) {
        this.httpTransportProvider = httpTransportProvider;
        return this;
    }

    public DrapiBuilder httpExecutor(Executor httpExecutor) {
        this.httpExecutor = httpExecutor;
        return this;
    }

    public Drapi build() {

        if (httpTransportProvider == null) {
            // Fallback to default HTTP transport provider if not provided
            // If HttpExecutor is not provided, it will be handled inside the relevant constructor
            this.httpTransportProvider = HttpTransportProvider.defaultTransportProvider();
        }

        // This is a bare transport context, which can be used to pass additional information to the transport layer if needed.
        HttpTransport httpTransport = httpTransportProvider.create(config, httpExecutor);
        TokenSource tokenSource = tokenSourceProvider.create(config, httpTransport);

        return new DrapiImpl(config, httpTransport, tokenSource);
    }

}
