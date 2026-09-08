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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openntf.drapi.exception.DrapiException;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.meta.Document;
import org.openntf.drapi.meta.ListEntry;

/**
 * Response Parser Test class to test the parsing various responses. This test needs a Json implementation to be present in the
 * classpath. So we moved this test to the drapi-sdk-test module which has a Json implementation dependency.
 */
class ResponseParserTest {

    private final Map<String, List<String>> mapWithCt = Map.of("Content-Type", List.of("application/json"));

    private DrapiResponse responseFromResource(int statusCode, Map<String, String> someHeaders, String resourcePath) {
        Map<String, List<String>> headers = new java.util.HashMap<>();
        if (someHeaders != null) {
            someHeaders.forEach((k, v) -> headers.put(k, List.of(v)));
        }

        headers.computeIfAbsent("Content-Type", k -> List.of("application/json"));

        return new DrapiResponse(statusCode, headers, getClass().getClassLoader().getResourceAsStream(resourcePath));
    }

    @Nested
    @DisplayName("Document Response Parsing Tests")
    class DocumentResponseParsingTests {

        @Test
        @DisplayName("Test @meta Parsing")
        void testMetaParsing() {

            try (var response = responseFromResource(200, null, "responses/document-response-1.json")) {
                Document doc = ResponseParser.toDocument(response);

                assertTrue(doc.meta().isPresent(), "Meta data should be present");
                assertFalse(doc.fieldNames().contains("@meta"), "@meta should be removed from the field names");
            }

        }

        @Test
        @DisplayName("Test No @meta case")
        void testNoMetaParsing() {
            try (var response = responseFromResource(200, null, "responses/document-response-2.json")) {
                Document doc = ResponseParser.toDocument(response);

                assertFalse(doc.meta().isPresent(), "Meta data should not be present");
            }

        }

        @Test
        @DisplayName("Test Warning Parsing")
        void testWarningParsing() {
            try (var response = responseFromResource(200, null, "responses/document-response-1.json")) {
                Document doc = ResponseParser.toDocument(response);

                assertFalse(doc.warnings().isEmpty(), "Warnings should not be empty");
                assertFalse(doc.fieldNames().contains("@warnings"), "@warnings should be removed from the field names");
            }
        }

        @Test
        @DisplayName("Test No Warning case")
        void testNoWarningParsing() {
            try (var response = responseFromResource(200, null, "responses/document-response-2.json")) {
                Document doc = ResponseParser.toDocument(response);

                assertTrue(doc.warnings().isEmpty(), "Warnings should be empty");
            }
        }

        @Test
        @DisplayName("Test with no form data")
        void testNoFormData() {
            byte[] jsonData = """
                {
                    "field1": "value1",
                    "field2": "value2"
                }
                """.getBytes(StandardCharsets.UTF_8);

            try (var is = new ByteArrayInputStream(jsonData);
                 var response = new DrapiResponse(200, mapWithCt, is)) {

                assertThrows(DrapiException.class, () -> ResponseParser.toDocument(response), "Expected DrapiException due to missing form data");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("Test case insensitivity for others fields")
        void testCaseInsensitivityForOtherFields() {
            try (var response = responseFromResource(200, null, "responses/document-response-1.json")) {
                Document doc = ResponseParser.toDocument(response);

                assertEquals("OpenNTF Domino API", doc.field("NaME").asString()
                                                      .orElse(null), "Field 'name' should be case-insensitive and return the correct value");
            }
        }

        @Test
        @DisplayName("Test null and empty fields")
        void testNullAndEmptyFields() {
            try (var response = responseFromResource(200, null, "responses/document-response-1.json")) {
                Document doc = ResponseParser.toDocument(response);

                assertEquals("", doc.field("details").asString()
                                    .orElse(null), "Field 'details' should return an empty string when the field is empty");
                assertTrue(doc.field("nonexistent").asString()
                              .isEmpty(), "Field 'nonexistent' should return an empty Optional when the field does not exist");
                assertFalse(doc.field("nonexistent")
                               .exists(), "Field 'nonexistent' should return non-present when the field does not exist");
                assertTrue(doc.field("nonexistent")
                              .isNull(), "Field 'nonexistent' should be marked as null when the field does not exist");
                assertTrue(doc.field("nullField")
                              .exists(), "Field 'nullField' should be present but should return an empty Optional when the field is null");
                assertTrue(doc.field("nullField").isNull(), "Field 'nullField' should be null when the field is null");
                assertTrue(doc.field("nullField").asString()
                              .isEmpty(), "Field 'nullField' should return empty Optional when the field is null");
            }
        }

        @Test
        @DisplayName("Test lists")
        void testLists() {
            try (var response = responseFromResource(200, null, "responses/document-response-1.json")) {
                Document doc = ResponseParser.toDocument(response);

                var chefsField = doc.field("chefs");
                assertTrue(chefsField.exists(), "Existing list field should be accessible and exists");
                assertTrue(chefsField.isMultiValue(), "Existing list field should be recognized as multi-value");
                assertEquals(3, chefsField.asList(String.class).size(), "Existing list field should have 3 elements");
                assertTrue(chefsField.asList(Integer.class)
                                     .isEmpty(), "Existing list field should return empty list when requested as wrong type");

                var emptyListField = doc.field("emptyList");
                assertTrue(emptyListField.exists(), "Empty list field should be accessible and exists");
                assertTrue(emptyListField.isMultiValue(), "Empty list field should be recognized as multi-value");
                assertTrue(emptyListField.asList(String.class).isEmpty(), "Empty list field should return an empty list");

                var nonExistentField = doc.field("nonExistentField");

                assertFalse(nonExistentField.exists(), "Non-existent field should not exist");
                assertFalse(nonExistentField.isMultiValue(), "Non-existent field should not be marked as multi-value");
            }
        }

        @Test
        @DisplayName("Test number access")
        void testNumberAccess() {
            try (var response = responseFromResource(200, null, "responses/document-response-1.json")) {
                Document doc = ResponseParser.toDocument(response);

                // Test number given in double format
                var downloadsDouble = doc.field("downloadsDouble");
                assertTrue(downloadsDouble.exists(), "Existing double number field should be accessible and exists");
                assertFalse(downloadsDouble.isMultiValue(), "Existing double number field should not be recognized as multi-value");
                assertEquals(16061, downloadsDouble.asInt()
                                                   .orElse(-1), "Existing double number field should return the correct integer value");
                assertEquals(16061, downloadsDouble.asLong()
                                                   .orElse(-1L), "Existing double number field should return the correct long value");
                assertEquals(16061.0, downloadsDouble.asDouble()
                                                     .orElse(-1.0), "Existing double number field should return the correct double value");

                // Test number given in integer format
                var downloadsInt = doc.field("downloadsInt");
                assertTrue(downloadsInt.exists(), "Existing integer number field should be accessible and exists");
                assertFalse(downloadsInt.isMultiValue(), "Existing integer number field should not be recognized as multi-value");
                assertEquals(16061, downloadsInt.asInt()
                                                .orElse(-1), "Existing integer number field should return the correct integer value");
                assertEquals(16061, downloadsInt.asLong()
                                                .orElse(-1L), "Existing integer number field should return the correct long value");
                assertEquals(16061.0, downloadsInt.asDouble()
                                                  .orElse(-1.0), "Existing integer number field should return the correct double value");

                // Test very large number given in integer format
                var largeNumberField = doc.field("largeNumber");
                assertTrue(largeNumberField.exists(), "Existing large number field should be accessible and exists");
                assertFalse(largeNumberField.isMultiValue(), "Existing large number field should not be recognized as multi-value");
                assertFalse(largeNumberField.asInt()
                                            .isPresent(), "Existing large number field should not return an integer value when it exceeds Integer.MAX_VALUE");
                assertEquals(2147483651L, largeNumberField.asLong()
                                                          .orElse(-1L), "Existing large number field should return the correct long value");
                assertEquals(2147483651.0, largeNumberField.asDouble()
                                                           .orElse(-1.0), "Existing large number field should return the correct double value");

                // Test decimal number given in double format
                var reallyDouble = doc.field("reallyDouble");
                assertTrue(reallyDouble.exists(), "Existing decimal number field should be accessible and exists");
                assertFalse(reallyDouble.isMultiValue(), "Existing decimal number field should not be recognized as multi-value");
                assertFalse(reallyDouble.asInt()
                                        .isPresent(), "Existing decimal number field should not return an integer value when it's not a whole number");
                assertFalse(reallyDouble.asLong()
                                        .isPresent(), "Existing decimal number field should not return a long value when it's not a whole number");
                assertEquals(3.1415926, reallyDouble.asDouble()
                                                    .orElse(-1.0), "Existing decimal number field should return the correct double value");

                // Test number given in string format
                var numberAsString = doc.field("numberAsString");
                assertTrue(numberAsString.exists(), "Existing number-as-string field should be accessible and exists");
                assertFalse(numberAsString.isMultiValue(), "Existing number-as-string field should not be recognized as multi-value");
                assertFalse(numberAsString.asInt()
                                          .isPresent(), "Existing number-as-string field should not return an integer value when it's a string");
                assertFalse(numberAsString.asLong()
                                          .isPresent(), "Existing number-as-string field should not return a long value when it's a string");
                assertFalse(numberAsString.asDouble()
                                          .isPresent(), "Existing number-as-string field should not return a double value when it's a string");
            }
        }

        @Test
        @DisplayName("Test Datetime access")
        void testDatetimeAccess() {
            try (var response = responseFromResource(200, null, "responses/document-response-1.json")) {
                var doc = ResponseParser.toDocument(response);

                var dateField = doc.field("dateField");
                assertTrue(dateField.exists(), "Existing date field should be accessible and exists");
                assertFalse(dateField.isMultiValue(), "Existing date field should not be recognized as multi-value");
                assertEquals("2026-09-04", dateField.asString()
                                                    .orElse(""), "Existing date field should return the correct string value");
                assertEquals(LocalDate.of(2026, 9, 4), dateField.asDate()
                                                                .orElse(null), "Existing date field should return the correct LocalDate value");

                var dateTimeField = doc.field("dateTimeField");
                assertTrue(dateTimeField.exists(), "Existing date-time field should be accessible and exists");
                assertFalse(dateTimeField.isMultiValue(), "Existing date-time field should not be recognized as multi-value");
                assertEquals("2026-09-04T07:47:59.28Z", dateTimeField.asString()
                                                                     .orElse(""), "Existing date-time field should return the correct string value");
                assertEquals(OffsetDateTime.of(2026, 9, 4, 7, 47, 59, 280_000_000, ZoneOffset.UTC), dateTimeField.asDateTime()
                                                                                                                 .orElse(null), "Existing date-time field should return the correct OffsetDateTime value");

                var invalidDateField = doc.field("invalidDateField");
                assertTrue(invalidDateField.exists(), "Invalid date field should be accessible and exists");
                assertFalse(invalidDateField.isMultiValue(), "Invalid date field should not be recognized as multi-value");
                assertEquals("2026-02-29", invalidDateField.asString().orElse(""), "Invalid date field should return a string value");
                assertFalse(invalidDateField.asDate().isPresent(), "Invalid date field should not return a LocalDate value");

                var invalidDateTimeField = doc.field("invalidDateTimeField");
                assertTrue(invalidDateTimeField.exists(), "Invalid date-time field should be accessible and exists");
                assertFalse(invalidDateTimeField.isMultiValue(), "Invalid date-time field should not be recognized as multi-value");
                assertEquals("2026-02-29T07:47:59.28Z", invalidDateTimeField.asString()
                                                                            .orElse(""), "Invalid date-time field should return a string value");
                assertFalse(invalidDateTimeField.asDateTime()
                                                .isPresent(), "Invalid date-time field should not return an OffsetDateTime value");

                var numberField = doc.field("largeNumber");
                assertTrue(numberField.asDate().isEmpty(), "Number field should not be convertible to LocalDate");
                assertTrue(numberField.asDateTime().isEmpty(), "Number field should not be convertible to OffsetDateTime");
            }
        }

        // TODO: Richtext field parsing test
        // TODO: Field Group parsing test

    }

    @Nested
    @DisplayName("List Entry Parsing Tests")
    class ListEntryParsingTests {

        @Test
        @DisplayName("ListTest with no content")
        void testNoFormData() {
            Map<String, Object> emptyMap = Map.of();

            ListEntry listEntry = ResponseParser.toListEntry(emptyMap);

            assertNotNull(listEntry, "Empty ListEntry can be created");
            assertTrue(listEntry.unid().isEmpty(), "UNID should be empty for empty ListEntry");
            assertTrue(listEntry.noteId().isEmpty(), "NoteID should be empty for empty ListEntry");
            assertTrue(listEntry.index().isEmpty(), "Index should be empty for empty ListEntry");
            assertTrue(listEntry.unread().isEmpty(), "Unread should be empty for empty ListEntry");
            assertTrue(listEntry.columnNames().isEmpty(), "Column names should be empty for empty ListEntry");
            assertTrue(listEntry.columns().findAny().isEmpty(), "Columns stream should be empty for empty ListEntry");
        }

        @Test
        @DisplayName("ListTest with content")
        void testWithContent() {
            Map<String, Object> contentMap = Map.of(
                "@unid", "12345",
                "@noteid", 67890,
                "@index", "1",
                "@unread", true,
                "column1", "value1",
                "column2", 42,
                "column3", List.of("item1", "item2"),
                "column4", "2026-09-04T07:47:59.28Z",
                "column5", "2026-09-04"
            );

            ListEntry listEntry = ResponseParser.toListEntry(contentMap);

            assertNotNull(listEntry, "ListEntry with content can be created");
            assertEquals("12345", listEntry.unid().orElse(null), "UNID should match the provided value");
            assertEquals(67890, listEntry.noteId().orElse(-1), "NoteID should match the provided value");
            assertEquals("1", listEntry.index().orElse(null), "Index should match the provided value");
            assertEquals(true, listEntry.unread().orElse(null), "Unread should match the provided value");

            assertEquals(Set.of("column1", "column2", "column3", "column4", "column5"), listEntry.columnNames(), "Column names should match the provided keys");
            assertEquals("value1", listEntry.column("column1").asString().orElse(null), "Column1 value should match");
            assertEquals(42, listEntry.column("column2").asInt().orElse(-1), "Column2 value should match");
            assertEquals(List.of("item1", "item2"), listEntry.column("column3").asList(String.class), "Column3 value should match");
            assertEquals("2026-09-04T07:47:59.28Z", listEntry.column("column4").asString().orElse(null), "Column4 value should match");
            assertTrue(listEntry.column("column4").asDateTime().isPresent(), "Column4 should be convertible to OffsetDateTime");
            assertEquals("2026-09-04", listEntry.column("column5").asString().orElse(null), "Column5 value should match");
            assertTrue(listEntry.column("column5").asDate().isPresent(), "Column5 should be convertible to LocalDate");
        }

    }
}
