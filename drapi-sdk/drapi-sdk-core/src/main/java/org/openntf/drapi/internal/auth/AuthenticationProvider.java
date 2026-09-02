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
package org.openntf.drapi.internal.auth;

import org.openntf.drapi.DrapiConfig;

public sealed interface AuthenticationProvider permits AuthenticationProviderBase {

    /**
     * Acquires a token for the given context. The implementation should not cache the token; the SDK will handle caching/refreshing.
     * <p>
     * This method is synchronous and returns the acquired BearerToken or throws an exception if the acquisition fails. We may consider
     * making this method asynchronous in the future, but for now, it is synchronous to simplify the implementation and usage.
     *
     * @param toolkit the toolkit providing context for token acquisition
     * @return a CompletableFuture that will complete with the acquired BearerToken, never {@code null}
     * @throws org.openntf.drapi.exception.AuthenticationException if a token could not be obtained
     */
    BearerToken acquireToken(AuthenticationToolkit toolkit);

    /**
     * Reports whether asking again after a 401 is worthwhile.
     * <p>
     * Return {@code false} for a provider handed a fixed, externally-obtained token: re-asking would return the same rejected value,
     * and failing immediately gives the caller a clearer error than a silent retry.
     *
     * @return whether the SDK should re-acquire after a 401, {@code true} by default
     */
    boolean supportsRefresh();

    /**
     * Factory method to create an appropriate AuthenticationProvider based on the DrapiConfig.
     *
     * @param config the DrapiConfig containing authentication type and related settings
     * @return an instance of AuthenticationProvider suitable for the specified authentication type
     */
    static AuthenticationProvider create(DrapiConfig config) {
        return switch (config.authType()) {
            case BASIC -> new BasicAuthenticationProvider(config);
            case TOKEN -> new TokenAuthenticationProvider(config);
            case OAUTH -> new OAuthAuthenticationProvider(config);
        };
    }

}
