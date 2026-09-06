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
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.internal.DrapiClientImpl;
import org.openntf.drapi.internal.DrapiContext;
import org.openntf.drapi.internal.auth.AuthenticationProvider;
import org.openntf.drapi.internal.auth.AuthenticationToolkit;
import org.openntf.drapi.internal.http.AuthenticatingHttpTransport;

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

        // authTransport is a wrapper around the provided HttpTransport that adds authentication capabilities
        AuthenticatingHttpTransport authTransport = new AuthenticatingHttpTransport(new AuthenticationToolkit(httpTransport), authenticationProvider);

        DrapiContext context = new DrapiContext(config, authTransport, authenticationProvider);
        return new DrapiClientImpl(context);
    }

}
