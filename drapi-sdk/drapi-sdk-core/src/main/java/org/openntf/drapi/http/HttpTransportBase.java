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

/**
 * Abstract base class for HTTP transport implementations. This class provides common functionality and configuration handling for
 * different HTTP transport mechanisms. Subclasses should implement the specific details of how to submit HTTP requests and handle
 * responses.
 * <p>
 * Since HttpTransport interface is sealed, this class is declared as non-sealed to allow for further subclassing by specific transport
 * implementations.
 */
public abstract non-sealed class HttpTransportBase implements HttpTransport {

    private static final Log LOG = Log.getLogger(HttpTransportBase.class);

    private final DrapiConfig config;
    private final Executor executor;

    /**
     * Constructor for HttpTransportBase. Initializes the transport with the provided configuration and executor.
     * <p>
     * One of the functionalities of this base class is to provide constructor injection for the DrapiConfig and Executor. This allows
     * subclasses to easily access configuration settings and manage
     * <p>
     * If Executor is not provided (i.e., null), the implementation can choose to use a default executor or handle asynchronous tasks in
     * a different manner. Whoever provides the Executor is responsible for its lifecycle management, including shutdown when no longer
     * needed.
     *
     * @param config   the DrapiConfig instance containing configuration settings for the transport
     * @param executor an optional Executor for managing asynchronous tasks; can be null, in which case the default executor will be
     *                 used
     */
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
        // Default implementation does nothing. Subclasses can override this method to provide specific stop behavior.
    }
}
