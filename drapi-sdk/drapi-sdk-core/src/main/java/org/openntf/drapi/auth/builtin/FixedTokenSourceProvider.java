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
package org.openntf.drapi.auth.builtin;

import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.auth.TokenSource;
import org.openntf.drapi.auth.TokenSourceProvider;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.util.TypeUtils;

public final class FixedTokenSourceProvider implements TokenSourceProvider {

    private String tokenValue = null;

    @Override
    public TokenSource create(DrapiConfig config, HttpTransport transport) {
        return new FixedTokenSource(config, transport, tokenValue);
    }

    /**
     * Creates a FixedTokenSourceProvider with the given token value.
     *
     * @param tokenValue the token value to use for authentication
     * @return a FixedTokenSourceProvider instance
     * @throws IllegalArgumentException if tokenValue is blank
     */
    public static FixedTokenSourceProvider withToken(String tokenValue) {
        // We want to give a clear error message if tokenValue is blank, so we check it here.
        TypeUtils.requireNonBlank(tokenValue, "tokenValue");

        FixedTokenSourceProvider provider = new FixedTokenSourceProvider();
        provider.tokenValue = tokenValue;
        return provider;
    }
}
