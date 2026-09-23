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

import org.openntf.drapi.util.TypeUtils;

/**
 * A bearer token is what SDK needs to authenticate with the server. For DRAPI, it must be a JWT, but the SDK doesn't enforce that. The
 * SDK only needs to know the token string.
 *
 * @param bearer the bearer token string
 * @param label  a label associated with the token (for logging and debugging)
 */
public record Token(String bearer, String label) {

    public Token {
        TypeUtils.requireNonEmpty(bearer, "bearer must not be null or empty");
        label = label == null ? "[no-label]" : label;
    }

    /**
     * Keeps the token out of logs and stack traces.
     *
     * @return a description with the token value redacted
     */
    @Override
    public String toString() {
        return "Token[label=" + label + ", bearer=<redacted>]";
    }
}
