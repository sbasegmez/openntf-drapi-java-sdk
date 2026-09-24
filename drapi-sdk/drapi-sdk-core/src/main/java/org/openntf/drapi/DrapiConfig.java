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
import java.util.Map;
import java.util.Optional;
import org.openntf.drapi.util.ConfigKey;

public interface DrapiConfig {

    /**
     * Returns the base URL for the API.
     *
     * @return the base URL
     */
    URI baseUrl();

    /**
     * Returns the user agent string for the API requests.
     *
     * @return the user agent string
     */
    String userAgent();

    /**
     * Returns the connection timeout in seconds for the API requests.
     *
     * @return the connection timeout in seconds
     */
    int connectTimeoutSecs();

    /**
     * Returns the request timeout in seconds for the API requests.
     *
     * @return the request timeout in seconds
     */
    int requestTimeoutSecs();

    /**
     * Returns the value of the specified configuration key.
     *
     * @param key the configuration key
     * @param <T> the type of the configuration value
     * @return an Optional containing the value of the configuration key, or an empty Optional if the key is not present
     */
    <T> Optional<T> get(ConfigKey<T> key);

    /**
     * Returns the value of the specified configuration key, or a default value if the key is not present.
     *
     * @param key          the configuration key
     * @param type         the type of the configuration value
     * @param <T>          the type of the configuration value
     * @return an Optional containing the value of the configuration key, or an empty Optional if the key is not present
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * Returns the value of the specified configuration key, or a default value if the key is not present.
     *
     * @param key          the configuration key
     * @param type         the type of the configuration value
     * @param defaultValue the default value to return if the key is not present
     * @param <T>          the type of the configuration value
     * @return an Optional containing the value of the configuration key, or an empty Optional if the key is not present
     */
    <T> Optional<T> get(String key, Class<T> type, T defaultValue);

    /**
     * Returns a new instance of the DrapiConfigBuilder.
     *
     * @return a new DrapiConfigBuilder instance
     */
    static DrapiConfigBuilder builder() {
        return new DrapiConfigBuilder();
    }

}
