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
import org.openntf.drapi.auth.SessionContext;
import org.openntf.drapi.auth.Token;
import org.openntf.drapi.auth.TokenSourceBase;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.util.ConfigKey;
import org.openntf.drapi.util.TypeUtils;

public final class FixedTokenSource extends TokenSourceBase {

    public static final ConfigKey<String> AUTH_TOKEN_KEY = ConfigKey.of("auth.token", String.class);

    private final Token token;

    /**
     * Creates a FixedTokenSource with the given configuration and transport. Since no token is provided, the token value is retrieved
     * from the configuration using the AUTH_TOKEN_KEY.
     * <p>
     * This constructor is the required one for the TokenSourceProvider to work properly, as it allows the token to be specified in the
     * configuration.
     *
     * @param config    the DrapiConfig instance containing the configuration
     * @param transport the HttpTransport instance for making HTTP requests
     */
    public FixedTokenSource(DrapiConfig config, HttpTransport transport) {
        this(config, transport, null);
    }

    /**
     * This is a convenience constructor to create a FixedTokenSource with the given configuration, transport, and token value. If the
     * token value is not provided, it will be retrieved from the configuration using the AUTH_TOKEN_KEY.
     *
     * @param config          the DrapiConfig instance containing the configuration
     * @param transport       the HttpTransport instance for making HTTP requests
     * @param givenTokenValue the token value to use, or null to retrieve it from the configuration
     */
    public FixedTokenSource(DrapiConfig config, HttpTransport transport, String givenTokenValue) {
        super(config, transport);

        String tokenValue = TypeUtils.defaultIfBlank(givenTokenValue, config.get(AUTH_TOKEN_KEY).orElse(null));

        if (TypeUtils.isBlank(tokenValue)) {
            throw new IllegalArgumentException("Auth token must be provided in the configuration for FixedTokenSource.");
        }

        this.token = new Token(tokenValue, "fixed-token");
    }

    @Override
    public Token acquire(SessionContext sessionContext) {
        return token;
    }

    @Override
    public void tokenRejected(SessionContext sessionContext, Token token) {
        // No action needed for fixed token
    }

    @Override
    public void logout(SessionContext sessionContext) {
        // No action needed for fixed token
    }

    @Override
    public boolean supportsRefresh() {
        return false;
    }
}
