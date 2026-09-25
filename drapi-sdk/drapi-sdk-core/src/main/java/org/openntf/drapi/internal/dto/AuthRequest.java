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
package org.openntf.drapi.internal.dto;

import java.util.HashMap;
import java.util.Map;
import org.openntf.drapi.json.JsonBinding;
import org.openntf.drapi.util.TypeUtils;

public record AuthRequest(String username, String password, String scope) {

    public static final String REQ_USERNAME = "username";
    public static final String REQ_PASSWORD = "password";
    public static final String REQ_SCOPE = "scope";

    public AuthRequest {
        TypeUtils.requireNonEmpty(username, "username must not be null or empty");
        TypeUtils.requireNonEmpty(password, "password must not be null or empty");
    }

    // We have an optional scope value, so we need to provide hard-coded converter
    public String toJson() {
        Map<String, String> map = new HashMap<>();
        map.put(REQ_USERNAME, username);
        map.put(REQ_PASSWORD, password);

        TypeUtils.ifNotBlank(scope, s -> map.put(REQ_SCOPE, s));

        return JsonBinding.get().toJson(map);
    }

    @Override
    public String toString() {
        return "AuthRequest{" +
            "username='" + username + '\'' +
            ", password='" + "********" + '\'' +
            ", scope='" + scope + '\'' +
            '}';
    }
}
