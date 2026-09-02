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
package org.openntf.drapi.json;

/**
 * SDK interface for JSON binding provider implementations. Implementations of this interface are expected to provide methods for
 * creating instances of JsonBinding.
 * <p>
 * This interface serves as a contract for JSON binding provider implementations, allowing for flexibility and interchangeability of
 * different JSON libraries or frameworks within the SDK.
 */
public interface JsonBindingProvider {

    /**
     * Creates a new instance of a JsonBinding implementation.
     *
     * @return a new JsonBinding instance
     */
    JsonBinding create();

}
