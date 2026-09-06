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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DocumentImplTest {

    // Tests:
    // - Test form cannot be empty
    // - Case insensitive field access

    @DisplayName("Test construction of DocumentImpl")
    @Test
    void testConstruction() {

        assertThrows(IllegalArgumentException.class, () -> new DocumentImpl(null, null, null, null), "Form name cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new DocumentImpl("", null, null, null), "Form name cannot be empty");
        assertThrows(NullPointerException.class, () -> new DocumentImpl("Test", null, null, null), "Value map cannot be null");

        var doc = new DocumentImpl("TestForm", null, null, Map.of("Field1", "Value1", "Field2", 123));

        assertTrue(doc.meta().isEmpty(), "Meta should be empty");
        assertTrue(doc.warnings().isEmpty(), "Warnings should be empty");

    }

    @Test
    @DisplayName("Test field access")
    void testFieldAccess() {
        Map<String, Object> valueMapWithNullField = new LinkedHashMap<>();
        valueMapWithNullField.put("Field2", 123);
        valueMapWithNullField.put("Field1", "Value1");
        valueMapWithNullField.put("Field3", null);

        var doc = new DocumentImpl("TestForm", null, null, valueMapWithNullField);

        assertEquals(Set.of("Field1", "Field2", "Field3"), doc.fieldNames(), "Field names should match, in case-insensitive order");
        assertEquals("Value1", doc.field("Field1").asString().orElse(""), "Field1 value should match");
        assertEquals(123, doc.field("Field2").asInt().orElse(0), "Field2 value should match");

        assertTrue(doc.field("Field3").isPresent(), "Field3 should be present");
        assertTrue(doc.field("Field3").isNull(), "Field3 should be null");

        assertFalse(doc.field("NonExistentField").isPresent(), "Non-existent field should not be present");
        assertFalse(doc.field("NonExistentField").raw().isPresent(), "Non-existent field should not have a value");
    }

    @Test
    @DisplayName("Test case-insensitive field access")
    void testCaseInsensitiveFieldAccess() {
        var doc = new DocumentImpl("TestForm", null, null, Map.of("Field1", "Value1", "FIELD2", 123));

        assertTrue(doc.field("field1").isPresent(), "Field1 should be accessible case-insensitively");
        assertEquals("Value1", doc.field("field1").asString().orElse(""), "Field1 value should match");
        assertTrue(doc.field("Field2").isPresent(), "FIELD2 should be accessible case-insensitively");
        assertEquals(123, doc.field("Field2").asInt().orElse(0), "Field2 value should match");
    }

}
