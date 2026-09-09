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

import java.util.Locale;

/**
 * Enum representing the type of key used in a list. Used by lists api.
 */
public enum KeyType {

    NUMBER,
    TEXT,
    TIME;

    /**
     * Returns the string representation of the key type.
     *
     * @return the string representation of the key type
     */
    public String value() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Returns the KeyType enum corresponding to the given string value.
     *
     * @param value the string representation of the key type
     * @return the corresponding KeyType enum
     * @throws IllegalArgumentException if the value does not match any KeyType
     */
    public static KeyType fromString(String value) {
        for (KeyType keyType : KeyType.values()) {
            if (keyType.name().equalsIgnoreCase(value)) {
                return keyType;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }

}
