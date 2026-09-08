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
package org.openntf.drapi.meta;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.openntf.drapi.internal.meta.DataTypeUtils;

/**
 * This interface provides fluent access (like Helidon Config API) to document fields or view columns, allowing for type-safe retrieval
 * of field values in various formats. It supports checking for presence, nullability, and multi-value status of fields, as well as
 * retrieving raw values and converting them to specific types.
 */
public sealed interface ValueHolder permits Field, Column {

    /**
     * Returns the name of the item, preserving the original casing as received from the server.
     *
     * @return the item name
     */
    String name();

    /**
     * Checks whether the value exists in the holder, regardless of its value.
     *
     * @return true if the item is present, false otherwise
     */
    boolean exists();

    /**
     * Returns the raw value of the item, which may be null if the item is not present or has a null value.
     *
     * @return the raw value of the item, or null if not present or has a null value
     */
    Object rawValue();

    /**
     * Returns the raw value, if present.
     *
     * @return an Optional containing the raw value, or empty if not present
     */
    default Optional<Object> raw() {
        return Optional.ofNullable(rawValue());
    }

    /**
     * Checks whether the item value is null.
     *
     * @return true if the item value is null, false otherwise
     */
    default boolean isNull() {
        return rawValue() == null;
    }

    /**
     * Checks whether the item contains multiple values.
     *
     * @return true if the value is a collection, false otherwise
     */
    default boolean isMultiValue() {
        return rawValue() instanceof Collection<?>;
    }

    /**
     * Returns the value as the specified type.
     * <p>
     * Currently, supported types: String, Integer, Long, Double, Boolean, OffsetDateTime, and LocalDate.
     *
     * @param type the class of the type to convert the field value to
     * @param <T>  the type to convert the field value to
     * @return an Optional containing the representation of the value as the specified type
     * @throws IllegalArgumentException if the conversion to the specified type is not supported
     */
    default <T> Optional<T> as(Class<T> type) {
        return DataTypeUtils.typedScalar(rawValue(), type);
    }

    /**
     * Returns the value as a list of the specified type.
     * <p>
     * Currently, supported types: String, Integer, Long, Double, Boolean, OffsetDateTime, and LocalDate.
     *
     * @param type the class of the type to convert the field values to
     * @param <T>  the type to convert the field values to
     * @return a List containing the representation of the values as the specified type
     * @throws IllegalArgumentException if the conversion to the specified type is not supported
     */
    default <T> List<T> asList(Class<T> type) {
        return DataTypeUtils.typedList(rawValue(), type).orElse(List.of());
    }

    /**
     * Returns the value as a String.
     *
     * @return an Optional containing the String representation of the value
     */
    default Optional<String> asString() {
        return as(String.class);
    }

    /**
     * Returns the value as an Integer.
     *
     * @return an Optional containing the Integer representation of the value
     */
    default Optional<Integer> asInt() {
        return as(Integer.class);
    }

    /**
     * Returns the value as a Long.
     *
     * @return an Optional containing the Long representation of the value
     */
    default Optional<Long> asLong() {
        return as(Long.class);
    }

    /**
     * Returns the value as a Double.
     *
     * @return an Optional containing the Double representation of the value
     */
    default Optional<Double> asDouble() {
        return as(Double.class);
    }

    /**
     * Returns the value as a Boolean.
     *
     * @return an Optional containing the Boolean representation of the value
     */
    default Optional<Boolean> asBoolean() {
        return as(Boolean.class);
    }

    /**
     * Returns the value as an OffsetDateTime.
     *
     * @return an Optional containing the OffsetDateTime representation of the value
     */
    default Optional<OffsetDateTime> asDateTime() {
        return as(OffsetDateTime.class);
    }

    /**
     * Returns the value as a LocalDate.
     *
     * @return an Optional containing the LocalDate representation of the value
     */
    default Optional<LocalDate> asDate() {
        return as(LocalDate.class);
    }

}
