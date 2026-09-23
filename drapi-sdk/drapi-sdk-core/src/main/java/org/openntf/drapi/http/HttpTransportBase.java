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
package org.openntf.drapi.http;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.internal.log.Log;

public abstract non-sealed class HttpTransportBase implements HttpTransport {

    private static final Log LOG = Log.getLogger(HttpTransportBase.class);

    private final DrapiConfig config;
    private final Executor executor;

    protected HttpTransportBase(DrapiConfig config, Executor executor) {
        this.config = Objects.requireNonNull(config, "config must not be null");

        // executor can be null, in which case the default executor will be picked up by the implementation
        this.executor = executor;
    }

    protected DrapiConfig config() {
        return config;
    }

    protected Optional<Executor> executor() {
        return Optional.ofNullable(executor);
    }

    @Override
    public void stop() {
        executor().ifPresent(executor -> {
            if (executor instanceof AutoCloseable ac) {
                try {
                    ac.close();
                } catch (Exception e) {
                    LOG.error("Failed to close executor", e);
                }
            }
        });
    }
}
