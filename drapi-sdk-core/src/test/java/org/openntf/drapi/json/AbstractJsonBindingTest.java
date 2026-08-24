package org.openntf.drapi.json;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openntf.drapi.exception.JsonBindingException;
import org.openntf.drapi.util.TypeUtils;

/**
 * We drop here a base class for JsonBinding tests to avoid having to repeat the same code in each test class. This class can be
 * extended by specific JSON binding test classes to inherit common test setup and utility methods.
 * <p>
 * All JSON binding implementations should pass this test class to achieve the expected behavior and functionality defined in the SDK.
 */
public abstract class AbstractJsonBindingTest {

    protected abstract JsonBinding jsonBinding();

    // Record for testing purposes
    public record Person(String name, int age, String city) {}


    @Test
    @DisplayName("Test that the JSON binding has a name")
    void testJsonBindingHasName() {
        String name = jsonBinding().name();
        assertTrue(TypeUtils.isNotBlank(name), "JSON binding name should not be blank");
    }

    @Nested
    @DisplayName("Test JSON binding fromJson method")
    class FromJsonTests {
        // Add test methods for the fromJson functionality here

        @Test
        @DisplayName("Test fromJson with valid JSON")
        void testFromJsonWithValidJson() {
            String jsonString = """
                {
                    "key": "value",
                    "integer": 42,
                    "double": 3.14,
                    "boolean": true,
                    "nullValue": null,
                    "array": [1, 2, 3],
                    "object": {
                        "nestedKey": "nestedValue"
                    }
                }""";

            var result = jsonBinding().fromJson(jsonString);

            assertTrue(result.containsKey("key"), "Result should contain the key 'key'");
            assertEquals("value", result.get("key"), "The value for 'key' should be 'value'");
            assertTrue(result.containsKey("integer"), "Result should contain the key 'integer'");
            assertEquals(42L, result.get("integer"), "The value for 'integer' should be 42 (Long)");
            assertTrue(result.containsKey("double"), "Result should contain the key 'double'");
            assertEquals(3.14, result.get("double"), "The value for 'double' should be 3.14");
            assertTrue(result.containsKey("boolean"), "Result should contain the key 'boolean'");
            assertEquals(true, result.get("boolean"), "The value for 'boolean' should be true");
            assertTrue(result.containsKey("nullValue"), "Result should contain the key 'nullValue'");
            assertNull(result.get("nullValue"), "The value for 'nullValue' should be null");
            assertTrue(result.containsKey("array"), "Result should contain the key 'array'");
            assertEquals(java.util.List.of(1L, 2L, 3L), result.get("array"), "The value for 'array' should be [1, 2, 3] (Long)");
            assertTrue(result.containsKey("object"), "Result should contain the key 'object'");
            assertEquals(java.util.Map.of("nestedKey", "nestedValue"), result.get("object"), "The value for 'object' should be {nestedKey=nestedValue}");
        }

        @Test
        @DisplayName("Test fromJson with empty JSON")
        void testFromJsonWithEmptyJson() {
            String jsonString = "{}";
            var result = jsonBinding().fromJson(jsonString);
            assertTrue(result.isEmpty(), "Result should be an empty map for empty JSON");
        }

        @Test
        @DisplayName("Test fromJson with invalid JSON")
        void testFromJsonWithInvalidJson() {
            String jsonString = "{invalidJson: true}";
            assertThrows(JsonBindingException.class, () -> jsonBinding().fromJson(jsonString), "Expected JsonBindingException for invalid JSON");
        }

        @Test
        @DisplayName("Test fromJson with non-object JSON")
        void testFromJsonWithNonObjectJson() {
            String jsonString = "[1, 2, 3]";
            assertThrows(JsonBindingException.class, () -> jsonBinding().fromJson(jsonString), "Expected JsonBindingException for non-object JSON");
        }

        @Test
        @DisplayName("Test fromJson with blank JSON string")
        void testFromJsonWithBlankJson() {
            assertThrows(JsonBindingException.class, () -> jsonBinding().fromJson(""), "Expected JsonBindingException for blank JSON string");
        }

        @Test
        @DisplayName("Test fromJson with null input")
        void testFromJsonWithNullInput() {
            assertThrows(NullPointerException.class, () -> jsonBinding().fromJson((String) null), "Expected NullPointerException for null input");
        }

        @Test
        @DisplayName("Test fromJson with null input stream")
        void testFromJsonWithNullInputStream() {
            assertThrows(NullPointerException.class, () -> jsonBinding().fromJson((java.io.InputStream) null), "Expected NullPointerException for null input stream");
        }

        @Test
        @DisplayName("Test insertion order preservation in fromJson")
        void testInsertionOrderPreservationInFromJson() {
            String jsonString = """
                {
                    "first": 1,
                    "second": 2,
                    "third": 3
                }""";

            var result = jsonBinding().fromJson(jsonString);
            var keys = result.keySet().toArray(new String[0]);
            assertEquals("first", keys[0], "First key should be 'first'");
            assertEquals("second", keys[1], "Second key should be 'second'");
            assertEquals("third", keys[2], "Third key should be 'third'");
        }
    }

    @Nested
    @DisplayName("Test JSON binding fromJson with type method")
    class FromJsonWithTypeTests {

        @Test
        @DisplayName("Test fromJson with type with valid JSON")
        void testFromJsonWithTypeWithValidJson() {
            String jsonString = """
                {
                    "name": "John Doe",
                    "age": 30
                }""";

            var result = jsonBinding().fromJson(jsonString, Person.class);

            assertEquals("John Doe", result.name(), "The name should be 'John Doe'");
            assertEquals(30, result.age(), "The age should be 30");
            assertNull(result.city(), "The city should be null since it's not provided in the JSON");
        }

        @Test
        @DisplayName("Test fromJson with type with invalid JSON")
        void testFromJsonWithTypeWithInvalidJson() {
            String jsonString = "{invalidJson: true}";
            assertThrows(JsonBindingException.class, () -> jsonBinding().fromJson(jsonString, Person.class), "Expected JsonBindingException for invalid JSON");
        }

        @Test
        @DisplayName("Test fromJson with type with null input")
        void testFromJsonWithTypeWithNullInput() {
            assertThrows(NullPointerException.class, () -> jsonBinding().fromJson((String) null, Person.class), "Expected NullPointerException for null input");
        }

        @Test
        @DisplayName("Test fromJson with type with null value type")
        void testFromJsonWithTypeWithNullValueType() {
            String jsonString = "{\"name\": \"John Doe\", \"age\": 30}";
            assertThrows(NullPointerException.class, () -> jsonBinding().fromJson(jsonString, null), "Expected NullPointerException for null value type");
        }

        @Test
        @DisplayName("Test fromJson with type with blank JSON string")
        void testFromJsonWithTypeWithBlankJson() {
            assertThrows(JsonBindingException.class, () -> jsonBinding().fromJson("", Person.class), "Expected JsonBindingException for blank JSON string");
        }

        @Test
        @DisplayName("Test fromJson with type with null input stream")
        void testFromJsonWithTypeWithNullInputStream() {
            assertThrows(NullPointerException.class, () -> jsonBinding().fromJson((java.io.InputStream) null, Person.class), "Expected NullPointerException for null input stream");
        }

        @Test
        @DisplayName("Test fromJson with type with non-object JSON")
        void testFromJsonWithTypeWithNonObjectJson() {
            String jsonString = "[1, 2, 3]";
            assertThrows(JsonBindingException.class, () -> jsonBinding().fromJson(jsonString, Person.class), "Expected JsonBindingException for non-object JSON");
        }

        @Test
        @DisplayName("Test fromJson with type with unmatched fields")
        void testFromJsonWithTypeWithUnmatchedFields() {
            String jsonString = """
                    {
                        "name": "Jane Doe",
                        "unmatchedField": "This field does not exist in the Person class"
                    }
                """;

            assertDoesNotThrow(() -> jsonBinding().fromJson(jsonString, Person.class), "Expected no exception for unmatched fields, they should be ignored");
        }
    }

    @Nested
    @DisplayName("Test JSON binding toJson method")
    class WriteToJsonTests {

        @Test
        @DisplayName("Test toJson with valid object")
        void testToJsonWithValidObject() {
            var person = new Person("Alice", 25, "Wonderland");
            String jsonString = jsonBinding().toJson(person);
            assertTrue(jsonString.contains("\"name\":\"Alice\""), "JSON string should contain the name field");
            assertTrue(jsonString.contains("\"age\":25"), "JSON string should contain the age field");
            assertTrue(jsonString.contains("\"city\":\"Wonderland\""), "JSON string should contain the city field");
        }

        @Test
        @DisplayName("Test toJson with null object")
        void testToJsonWithNullObject() {
            assertThrows(NullPointerException.class, () -> jsonBinding().toJson(null), "Expected NullPointerException for null object");
        }

        @Test
        @DisplayName("Test toJson with object containing null fields")
        void testToJsonWithObjectContainingNullFields() {
            var person = new Person("Bob", 40, null);
            String jsonString = jsonBinding().toJson(person);
            assertTrue(jsonString.contains("\"name\":\"Bob\""), "JSON string should contain the name field");
            assertTrue(jsonString.contains("\"age\":40"), "JSON string should contain the age field");
            assertTrue(jsonString.contains("\"city\":null"), "JSON string should contain the city field with null value");
        }
    }

}
