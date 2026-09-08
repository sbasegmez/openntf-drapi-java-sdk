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

public enum QueryScope {

    ALL,
    CATEGORIES,
    DOCUMENTS;

    public String value() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    public static QueryScope of(String value) {
        for (QueryScope scope : QueryScope.values()) {
            if (scope.name().equalsIgnoreCase(value)) {
                return scope;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }

}
