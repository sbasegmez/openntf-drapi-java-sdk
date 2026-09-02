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
package org.openntf.drapi.internal.auth;

import java.util.Objects;
import org.openntf.drapi.http.HttpTransport;

/**
 * A contextual toolkit for authentication operations.
 * <p>
 * This record encapsulates the necessary components for performing authentication, including an HttpTransport for making HTTP requests.
 * This class has only one component for now, but it is designed to be extensible in the future to include additional components as needed.
 */
public record AuthenticationToolkit(HttpTransport httpTransport) {

    /**
     * Creates a new instance of AuthenticationToolkit with the specified HttpTransport.
     *
     * @param httpTransport the bare (non-authenticating) HttpTransport to be used for authentication operations.
     */
    public AuthenticationToolkit {
        Objects.requireNonNull(httpTransport, "httpTransport must not be null");
    }

}
