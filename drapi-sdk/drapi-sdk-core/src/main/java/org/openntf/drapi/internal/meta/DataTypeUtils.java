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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DataTypeUtils {

    private DataTypeUtils() {
        // Private constructor to prevent instantiation
    }

    public static <T> Optional<T> typedScalar(Object value, Class<T> type) {
        Objects.requireNonNull(type, "Type must not be null");

        if (value == null) {
            return Optional.empty();
        }

        if (type == String.class) {
            return toString(value).map(type::cast);
        }
        if (type == Integer.class) {
            return toInteger(value).map(type::cast);
        }
        if (type == Long.class) {
            return toLong(value).map(type::cast);
        }
        if (type == Double.class) {
            return toDouble(value).map(type::cast);
        }
        if (type == Boolean.class) {
            return toBoolean(value).map(type::cast);
        }
        if (type == OffsetDateTime.class) {
            return toDateTime(value).map(type::cast);
        }
        if (type == LocalDate.class) {
            return toDate(value).map(type::cast);
        }

        // TODO Add Custom type conversion support

        return Optional.empty(); // Add more type conversions as needed
    }

    public static <T> Optional<List<T>> typedList(Object value, Class<T> type) {
        List<T> resultList = new ArrayList<>();

        if(value instanceof List<?> list) {
            for(Object item : list) {
                Optional<T> typedItem = typedScalar(item, type);
                if(typedItem.isPresent()) {
                    resultList.add(typedItem.get());
                } else {
                    return Optional.empty(); // If any item cannot be converted, return empty
                }
            }

            return Optional.of(resultList);
        } else if (type.isInstance(value)) {
            // If the value is a single instance of the desired type, wrap it in a list
            resultList.add(type.cast(value));
            return Optional.of(resultList);
        }

        return Optional.empty();
    }


    public static Optional<String> toString(Object value) {
        if(value instanceof String strValue) {
            return Optional.of(strValue);
        }

        return Optional.empty();
    }

    public static Optional<Integer> toInteger(Object value) {
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof Integer intValue) {
            return Optional.of(intValue);
        }

        if (value instanceof Long longValue) {
            int intValue = longValue.intValue();

            if (intValue == longValue) {
                return Optional.of(intValue);
            } else {
                return Optional.empty();
            }

        }

        if (value instanceof Double doubleValue) {
            int intValue = doubleValue.intValue();

            if (intValue == doubleValue) {
                return Optional.of(intValue);
            } else {
                return Optional.empty();
            }
        }

        // We don't expect BigDecimal or BigInteger here, but if you want to handle them, you can add checks for those types as well.
        // Also we don't want to mutate data types, so we won't convert from String to Integer here.

        return Optional.empty();
    }

    public static Optional<Long> toLong(Object value) {
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof Integer intValue) {
            return Optional.of(Long.valueOf(intValue));
        }

        if (value instanceof Long longValue) {
            return Optional.of(longValue);
        }

        if (value instanceof Double doubleValue) {
            long longValue = doubleValue.longValue();

            if (longValue == doubleValue) {
                return Optional.of(longValue);
            } else {
                return Optional.empty();
            }
        }

        // We don't expect BigDecimal or BigInteger here, but if you want to handle them, you can add checks for those types as well.
        // Also we don't want to mutate data types, so we won't convert from String to Long here.

        return Optional.empty();
    }

    public static Optional<Double> toDouble(Object value) {
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof Integer intValue) {
            return Optional.of(Double.valueOf(intValue));
        }

        if (value instanceof Long longValue) {
            return Optional.of(Double.valueOf(longValue));
        }

        if (value instanceof Double doubleValue) {
            return Optional.of(doubleValue);
        }

        // We don't expect BigDecimal or BigInteger here, but if you want to handle them, you can add checks for those types as well.
        // Also we don't want to mutate data types, so we won't convert from String to Double here.

        return Optional.empty();
    }

    public static Optional<Boolean> toBoolean(Object value) {
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof Boolean) {
            return Optional.of((Boolean) value);
        }

        return Optional.empty();
    }

    /**
     * Converts the given value to an Optional containing an OffsetDateTime if possible.
     * <p>
     * In fact, DRAPI almost always return UTC (2026-01-01T10:49:2Z). So we could use Instant as well. But OffsetDateTime is more
     * flexible and can handle different time zones if needed in the future.
     *
     * @param value the value to convert
     * @return an Optional containing the OffsetDateTime if conversion is successful, or empty if not
     */
    public static Optional<OffsetDateTime> toDateTime(Object value) {
        Optional<String> stringValue = toString(value);

        if (stringValue.isPresent()) {
            try {
                return Optional.of(OffsetDateTime.parse(stringValue.get()));
            } catch (Exception e) {
                // Ignore parsing exceptions and return empty Optional
            }
        }

        return Optional.empty();
    }

    public static Optional<LocalDate> toDate(Object value) {
        Optional<String> stringValue = toString(value);

        if (stringValue.isPresent()) {
            try {
                return Optional.of(LocalDate.parse(stringValue.get()));
            } catch (Exception e) {
                // Ignore parsing exceptions and return empty Optional
            }
        }

        return Optional.empty();
    }

}
