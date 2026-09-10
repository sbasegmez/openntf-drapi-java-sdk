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
package org.openntf.drapi.api.options;

/**
 * Enum representing the direction of sorting, either ascending or descending. Used by lists api.
 */
public enum Direction {
    ASCENDING("asc"),
    DESCENDING("desc");

    private final String value;

    Direction(String value) {
        this.value = value;
    }

    /**
     * Returns the string representation of the direction.
     *
     * @return the string representation of the direction
     */
    public String value() {
        return value;
    }

    /**
     * Returns the Direction enum corresponding to the given string value.
     *
     * @param value the string representation of the direction
     * @return the corresponding Direction enum
     * @throws IllegalArgumentException if the value does not match any Direction
     */
    public static Direction of(String value) {
        for (Direction direction : Direction.values()) {
            if (direction.value.equalsIgnoreCase(value)) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Unknown direction value: " + value);
    }
}
