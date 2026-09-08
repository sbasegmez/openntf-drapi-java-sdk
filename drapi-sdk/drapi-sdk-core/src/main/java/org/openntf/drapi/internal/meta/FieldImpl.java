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
package org.openntf.drapi.internal.meta;

import org.openntf.drapi.meta.Field;

public final class FieldImpl implements Field {

    private final String name;
    private final Object rawValue;
    private final boolean exists;

    public FieldImpl(String name, Object rawValue, boolean exists) {
        this.name = name;
        this.rawValue = rawValue;
        this.exists = exists;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public boolean exists() {
        return this.exists;
    }

    @Override
    public Object rawValue() {
        return this.rawValue;
    }

}
