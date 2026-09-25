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

public final class PasswordTokenSourceProvider implements TokenSourceProvider {

    private String username = null;
    private String password = null;
    private String scope = null;

    @Override
    public TokenSource create(DrapiConfig config, HttpTransport transport) {
        return new PasswordTokenSource(config, transport, username, password, scope);
    }

    /**
     * Creates a PasswordTokenSourceProvider with the given username and password.
     *
     * @param username the username to use for authentication
     * @param password the password to use for authentication
     * @return a PasswordTokenSourceProvider instance
     * @throws IllegalArgumentException if username or password is blank
     */
    public static PasswordTokenSourceProvider withCredentials(String username, String password) {
        return withCredentials(username, password, null);
    }

    /**
     * Creates a PasswordTokenSourceProvider with the given username, password, and scope.
     *
     * @param username the username to use for authentication
     * @param password the password to use for authentication
     * @param scope the scope to use for authentication
     * @return a PasswordTokenSourceProvider instance
     * @throws IllegalArgumentException if username or password is blank
     */
    public static PasswordTokenSourceProvider withCredentials(String username, String password, String scope) {
        // We want to give a clear error message if username or password is blank, so we check them here.
        TypeUtils.requireNonBlank(username, "username cannot be blank or null");
        TypeUtils.requireNonBlank(password, "password cannot be blank or null");

        PasswordTokenSourceProvider provider = new PasswordTokenSourceProvider();
        provider.username = username;
        provider.password = password;
        provider.scope = scope;
        return provider;
    }

}
