package org.openntf.drapi.internal.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataTypeUtilsTest {

    @Test
    @DisplayName("Test typedScalar")
    void testTypedScalar() {
        assertTrue(DataTypeUtils.typedScalar(null, String.class).isEmpty(), "Null value should return empty Optional");
        assertTrue(DataTypeUtils.typedScalar("test", NonExistentType.class).isEmpty(), "Unsupported type should return empty Optional");
    }

    @Test
    @DisplayName("Test typedList")
    void testTypedList() {
        assertTrue(DataTypeUtils.typedList(null, String.class).isEmpty(), "Null value should return empty Optional");

        assertEquals(List.of("test"), DataTypeUtils.typedList("test", String.class).orElse(List.of()), "Non-list value should return a list with the single element");
        assertEquals(List.of("test1", "test2"), DataTypeUtils.typedList(List.of("test1", "test2"), String.class).orElse(List.of()), "List of strings should return the same list");
        assertEquals(List.of(123, 456), DataTypeUtils.typedList(List.of(123, 456), Integer.class).orElse(List.of()), "List of integers should return the same list");

        assertTrue(DataTypeUtils.typedList(List.of("test1", "test2"), NonExistentType.class).isEmpty(), "Unsupported type should return empty Optional");
        assertTrue(DataTypeUtils.typedList(List.of("test1", 123), String.class).isEmpty(), "Mixed type list should return empty Optional");
        assertTrue(DataTypeUtils.typedList("test", Integer.class).isEmpty(), "Non-list value of unsupported type should return empty Optional");
    }

    @Test
    @DisplayName("Test String conversion")
    void testStringConversion() {
        assertEquals("test", DataTypeUtils.toString("test").orElse("NO_VALUE"));
        assertNotEquals("123", DataTypeUtils.toString(123).orElse("NO_VALUE"), "toString should not convert Integer to String");
        assertNotEquals("true", DataTypeUtils.toString(true).orElse("NO_VALUE"), "toString should not convert Boolean to String");
        assertNull(DataTypeUtils.toString(null).orElse(null));
    }

    @Test
    @DisplayName("Test null value handling")
    void testNullValueHandling() {
        assertTrue(DataTypeUtils.typedScalar(null, String.class).isEmpty(), "Null value should return empty Optional");
        assertThrows(NullPointerException.class, () -> DataTypeUtils.typedScalar("test", null));
    }

    @Test
    @DisplayName("Test Integer conversion")
    void testIntegerConversion() {
        assertEquals(123, DataTypeUtils.toInteger(123).orElse(-1));

        Long longValue = 456L;
        assertEquals(456, DataTypeUtils.toInteger(longValue).orElse(-1));

        Double doubleValue = 789.0;
        assertEquals(789, DataTypeUtils.toInteger(doubleValue).orElse(0));

        Double doubleValueWithFraction = 123.45;
        assertTrue(DataTypeUtils.toInteger(doubleValueWithFraction).isEmpty(), "Double with fraction should not convert to Integer");

        Long longValueOutOfRange = (long) Integer.MAX_VALUE + 1;
        assertTrue(DataTypeUtils.toInteger(longValueOutOfRange).isEmpty(), "Long value out of Integer range should not convert to Integer");

        assertTrue(DataTypeUtils.toInteger("456").isEmpty(), "toInteger should not convert String to Integer");
    }

    @Test
    @DisplayName("Test Long conversion")
    void testLongConversion() {
        Integer intValue = 123;
        assertEquals(123L, DataTypeUtils.toLong(intValue).orElse(-1L));

        Long longValue = 456L;
        assertEquals(456L, DataTypeUtils.toLong(longValue).orElse(-1L));

        Double doubleValue = 789.0;
        assertEquals(789L, DataTypeUtils.toLong(doubleValue).orElse(0L));

        Double doubleValueWithFraction = 123.45;
        assertTrue(DataTypeUtils.toLong(doubleValueWithFraction).isEmpty(), "Double with fraction should not convert to Long");

        Double doubleValueOutOfRange = Long.MAX_VALUE * 2.0;
        assertTrue(DataTypeUtils.toLong(doubleValueOutOfRange).isEmpty(), "Double value out of long range should not convert to Long");

        assertTrue(DataTypeUtils.toLong("456").isEmpty(), "toLong should not convert String to Long");
    }

    @Test
    @DisplayName("Test Double conversion")
    void testDoubleConversion() {
        Integer intValue = 123;
        assertEquals(123, DataTypeUtils.toDouble(intValue).orElse(-1.0));

        Long longValue = 456L;
        assertEquals(456.0, DataTypeUtils.toDouble(longValue).orElse(-1.0));

        Double doubleValue = 789.0;
        assertEquals(789.0, DataTypeUtils.toDouble(doubleValue).orElse(0.0));

        Double doubleValueWithFraction = 123.45;
        assertEquals(123.45, DataTypeUtils.toDouble(doubleValueWithFraction).orElse(0.0));

        assertTrue(DataTypeUtils.toDouble("456.05").isEmpty(), "toDouble should not convert String to Double");
    }

    @Test
    @DisplayName("Test Boolean conversion")
    void testBooleanConversion() {
        assertEquals(true, DataTypeUtils.toBoolean(true).orElse(false));
        assertEquals(false, DataTypeUtils.toBoolean(false).orElse(true));

        assertTrue(DataTypeUtils.toBoolean("true").isEmpty(), "toBoolean should not convert String to Boolean");
        assertTrue(DataTypeUtils.toBoolean(1).isEmpty(), "toBoolean should not convert Integer to Boolean");
    }

    @Test
    @DisplayName("Test DateTime conversion")
    void testDateTimeConversion() {
        OffsetDateTime odtWithOffset = OffsetDateTime.parse("2026-09-04T19:49:02+01:00");
        assertEquals(odtWithOffset, DataTypeUtils.toDateTime("2026-09-04T19:49:02+01:00").orElse(null), "toDateTime should convert valid ISO-8601 string with offset to OffsetDateTime");

        OffsetDateTime odtNoOffset = OffsetDateTime.parse("2026-09-04T19:49:02Z");
        assertEquals(odtNoOffset, DataTypeUtils.toDateTime("2026-09-04T19:49:02Z").orElse(null), "toDateTime should convert valid ISO-8601 string without offset to OffsetDateTime");

        assertTrue(DataTypeUtils.toDateTime("invalid-date").isEmpty(), "toDateTime should return empty Optional for invalid date string");
    }

    @Test
    @DisplayName("Test Date conversion")
    void testDateConversion() {
        LocalDate date = LocalDate.parse("2026-09-04");
        assertEquals(date, DataTypeUtils.toDate("2026-09-04").orElse(null), "toDate should convert valid ISO-8601 date string to LocalDate");

        assertTrue(DataTypeUtils.toDate("invalid-date").isEmpty(), "toDate should return empty Optional for invalid date string");
        assertTrue(DataTypeUtils.toDate("2026-09-04T19:49:02+01:00").isEmpty(), "toDate should return empty Optional for date-time string");
    }

    static class NonExistentType {}
}
