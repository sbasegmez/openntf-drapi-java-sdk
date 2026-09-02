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
package org.openntf.drapi.internal.http.jdk;

import java.util.concurrent.Executor;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.http.HttpTransportProvider;

public class JdkHttpTransportProvider implements HttpTransportProvider {

    @Override
    public HttpTransport create(DrapiConfig config, Executor executor) {
        return new JdkHttpTransport(config, executor);
    }
}
