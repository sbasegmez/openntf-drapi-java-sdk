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
package org.openntf.drapi.auth;

import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.http.HttpTransport;

public abstract non-sealed class TokenSourceBase implements TokenSource {

    protected final DrapiConfig config;
    protected final HttpTransport transport;

    protected TokenSourceBase(DrapiConfig config, HttpTransport transport) {
        this.config = config;
        this.transport = transport;
    }

    protected DrapiConfig config() {
        return this.config;
    }

    protected HttpTransport transport() {
        return this.transport;
    }

}
