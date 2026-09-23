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

import java.util.concurrent.Executor;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.internal.http.jdk.JdkHttpTransportProvider;
import org.openntf.drapi.util.ServiceRegistry;

public interface HttpTransportProvider {

    /**
     * Creates a new instance of HttpTransport based on the provided DrapiConfig and Executor.
     *
     * @param config the DrapiConfig containing configuration and settings for the transport
     * @param executor an optional Executor to be used by the transport
     * @return a new instance of HttpTransport
     */
    HttpTransport create(DrapiConfig config, Executor executor);

    /**
     * Checks if there is an SPI-based implementation of HttpTransportProvider available, and returns it if found. Otherwise, it returns
     * the default implementation (JdkHttpTransportProvider).
     *
     * @return the default HttpTransport implementation
     */
    static HttpTransportProvider defaultTransportProvider() {
        return ServiceRegistry.findServiceOrDefault(HttpTransportProvider.class, JdkHttpTransportProvider::new);
    }
}
