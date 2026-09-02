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
package org.openntf.json.jakarta;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openntf.drapi.json.AbstractJsonBindingTest;
import org.openntf.drapi.json.JsonBinding;

class JakartaJsonBindingTest extends AbstractJsonBindingTest {

    private final static JakartaJsonBinding JSON_BINDING = new JakartaJsonBinding();

    @Override
    protected JsonBinding jsonBinding() {
        return JSON_BINDING;
    }

    @Test
    @DisplayName("Test that the JakartaJsonBinding is detected as the default JSON binding")
    void testJsonBindingIsDetected() {
        // Ensure that the JakartaJsonBinding is detected as the default JSON binding
        JsonBinding binding = JsonBinding.get();

        assertNotNull(binding, "Default JSON binding should not be null");
        assertSame(JakartaJsonBinding.class, binding.getClass(), "Default JSON binding should be JakartaJsonBinding");
    }

}
