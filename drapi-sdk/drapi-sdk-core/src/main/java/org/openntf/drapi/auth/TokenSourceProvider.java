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

import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.http.HttpTransport;

/**
 * TokenSourceProvider is an interface that defines a method for creating instances of TokenSource.
 * <p>
 * Implementations of this interface should provide a concrete implementation of the create method to instantiate and return a
 * TokenSource based on the provided DrapiConfig and HttpTransport.
 * <p>
 * Implementations must have a public no-argument constructor to allow for instantiation.
 */
public interface TokenSourceProvider {

    /**
     * Creates a TokenSource instance based on the provided DrapiConfig and HttpTransport.
     * <p>
     * Implementations of any TokenSource should provide a concrete implementation of this method to create and return a TokenSource
     * instance. So we can guarantee the TokenSource is created with the correct configuration and transport.
     *
     * @param config    DrapiConfig containing configuration settings for the TokenSource
     * @param transport HttpTransport used for making network requests
     * @return a TokenSource instance
     */
    TokenSource create(DrapiConfig config, HttpTransport transport);

}
