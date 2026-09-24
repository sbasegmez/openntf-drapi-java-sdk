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

import java.net.URI;
import java.util.Optional;
import org.openntf.drapi.util.ConfigKey;

public interface DrapiConfig {

    URI baseUrl();

    String userAgent();

    int connectTimeoutSecs();

    int requestTimeoutSecs();

    <T> Optional<T> get(ConfigKey<T> key);

    <T> Optional<T> get(String key, Class<T> type);

    <T> Optional<T> get(String key, Class<T> type, T defaultValue);

    static DrapiConfigBuilder builder() {
        return new DrapiConfigBuilder();
    }

}
