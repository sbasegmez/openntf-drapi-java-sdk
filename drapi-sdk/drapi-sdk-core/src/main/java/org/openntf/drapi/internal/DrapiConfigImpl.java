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
package org.openntf.drapi.internal;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.DrapiConfigBuilder;
import org.openntf.drapi.util.ConfigKey;
import org.openntf.drapi.util.TypeUtils;

public class DrapiConfigImpl implements DrapiConfig {

    public static final String DEFAULT_USER_AGENT = "OPENNTF-DRAPI-SDK-JAVA";
    public static final int DEFAULT_CONNECT_TIMEOUT_SECS = 5;
    public static final int DEFAULT_REQUEST_TIMEOUT_SECS = 15;

    // Baseline
    private final URI baseUrl;

    // Version tag will be appended in the constructor
    private final String userAgent;

    // Network timeouts
    private final int connectTimeoutSecs;
    private final int requestTimeoutSecs;

    // Arbitrary parameters
    private final Map<String, Object> extraParams;

    public DrapiConfigImpl(DrapiConfigBuilder builder) {
        this.baseUrl = Objects.requireNonNull(builder.baseUrl(), "baseUrl cannot be null");
        this.extraParams = builder.extraParams();

        this.userAgent = TypeUtils.defaultIfBlank(builder.userAgent(), DEFAULT_USER_AGENT + "/" + Version.get());
        this.connectTimeoutSecs = builder.connectTimeoutSecs() == 0 ? DEFAULT_CONNECT_TIMEOUT_SECS : builder.connectTimeoutSecs();
        this.requestTimeoutSecs = builder.requestTimeoutSecs() == 0 ? DEFAULT_REQUEST_TIMEOUT_SECS : builder.requestTimeoutSecs();
    }

    @Override
    public URI baseUrl() {
        return baseUrl;
    }

    @Override
    public String userAgent() {
        return userAgent;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = extraParams.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not of type " + type.getName());
    }

    @Override
    public <T> Optional<T> get(ConfigKey<T> key) {
        return get(key.key(), key.type());
    }

    @Override
    public int connectTimeoutSecs() {
        return connectTimeoutSecs;
    }

    @Override
    public int requestTimeoutSecs() {
        return requestTimeoutSecs;
    }
}
