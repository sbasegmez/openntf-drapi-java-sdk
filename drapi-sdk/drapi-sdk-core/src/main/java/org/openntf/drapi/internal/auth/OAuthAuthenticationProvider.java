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

public final class OAuthAuthenticationProvider extends AuthenticationProviderBase {

    public OAuthAuthenticationProvider(DrapiConfig config) {
        super(config);
        throw new UnsupportedOperationException("OAuth authentication is not yet implemented.");
    }

    @Override
    public BearerToken acquireToken(AuthenticationToolkit toolkit) {
        return null;
    }

    @Override
    public boolean supportsRefresh() {
        return false;
    }
}
