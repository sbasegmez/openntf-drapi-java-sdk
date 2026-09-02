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
