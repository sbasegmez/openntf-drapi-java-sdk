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

import java.util.Objects;
import org.openntf.drapi.DrapiClient;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.DrapiContext;

public class DrapiClientImpl implements DrapiClient {

    private final DrapiContext context;

    DrapiClientImpl(DrapiConfig config, DrapiContext context) {
        this.context = Objects.requireNonNull(context, "Context must not be null");
    }

    DrapiContext context() {
        return context;
    }

    DrapiConfig config() {
        return context.config();
    }

}
