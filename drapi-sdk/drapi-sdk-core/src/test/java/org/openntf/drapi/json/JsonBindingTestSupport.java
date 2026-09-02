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
 * Test support class for JsonBinding.
 * <p>
 * Test classes using JsonBinding can set a mock or custom implementation of JsonBinding for testing purposes. Set the json binding
 * instance on @BeforeEach and reset it on @AfterEach to ensure that the test does not affect other tests.
 */
public class JsonBindingTestSupport {

    public static void set(JsonBinding jsonBinding) {
        JsonBinding.JsonBindingHolder.override(jsonBinding);
    }

    public static void reset() {
        JsonBinding.JsonBindingHolder.reset();
    }

}
