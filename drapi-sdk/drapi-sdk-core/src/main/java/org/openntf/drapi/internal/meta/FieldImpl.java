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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.openntf.drapi.meta.Field;

public class FieldImpl implements Field {

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
    public boolean isNull() {
        return rawValue == null;
    }

    @Override
    public boolean isMultiValue() {
        return rawValue instanceof Collection<?>;
    }

    @Override
    public Optional<Object> raw() {
        return Optional.ofNullable(rawValue);
    }

    @Override
    public <T> Optional<T> as(Class<T> type) {
        return DataTypeUtils.typedScalar(rawValue, type);
    }

    @Override
    public <T> List<T> asList(Class<T> type) {
        return DataTypeUtils.typedList(rawValue, type).orElse(List.of());
    }

    @Override
    public Optional<String> asString() {
        return as(String.class);
    }

    @Override
    public Optional<Integer> asInt() {
        return as(Integer.class);
    }

    @Override
    public Optional<Long> asLong() {
        return as(Long.class);
    }

    @Override
    public Optional<Double> asDouble() {
        return as(Double.class);
    }

    @Override
    public Optional<Boolean> asBoolean() {
        return as(Boolean.class);
    }

    @Override
    public Optional<OffsetDateTime> asDateTime() {
        return as(OffsetDateTime.class);
    }

    @Override
    public Optional<LocalDate> asDate() {
        return as(LocalDate.class);
    }
}
