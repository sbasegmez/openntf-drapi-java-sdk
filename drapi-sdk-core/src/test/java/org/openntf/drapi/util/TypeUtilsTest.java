package org.openntf.drapi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

class TypeUtilsTest {

    @Test
    @DisplayName("Test isEmpty and isNotEmpty methods")
    void isEmptyTests() {
        assertTrue(TypeUtils.isEmpty(""));
        assertFalse(TypeUtils.isEmpty(" "));
        assertFalse(TypeUtils.isEmpty("a"));

        assertTrue(TypeUtils.isEmpty((Object) null));

        // Collections
        assertTrue(TypeUtils.isEmpty(List.of()));
        assertFalse(TypeUtils.isEmpty(List.of("a")));
    }

    @Test
    @DisplayName("Test isBlank method")
    void isBlankTests() {
        assertTrue(TypeUtils.isBlank(""));
        assertTrue(TypeUtils.isBlank(null));
        assertTrue(TypeUtils.isBlank(" "));
        assertFalse(TypeUtils.isBlank("a"));
    }

    @Test
    @DisplayName("Test isNumeric method")
    void isNumeric() {
        assertTrue(TypeUtils.isNumeric("123"));
        assertTrue(TypeUtils.isNumeric(" 123"));
        assertTrue(TypeUtils.isNumeric(" 123 "));
        assertTrue(TypeUtils.isNumeric("0"));

        assertFalse(TypeUtils.isNumeric(null));
        assertFalse(TypeUtils.isNumeric(""));
        assertFalse(TypeUtils.isNumeric(" "));
        assertFalse(TypeUtils.isNumeric("a"));
        assertFalse(TypeUtils.isNumeric("12 3"));
        assertFalse(TypeUtils.isNumeric("ab2c"));
        assertFalse(TypeUtils.isNumeric("12-3"));
        assertFalse(TypeUtils.isNumeric("1.2"));
        assertFalse(TypeUtils.isNumeric("1,2"));
        assertFalse(TypeUtils.isNumeric("+12"));
        assertFalse(TypeUtils.isNumeric("-12"));
    }

    @Test
    void equalsOneOf() {
        assertTrue(TypeUtils.equalsOneOf("a", "a", "b", "c"));
        assertTrue(TypeUtils.equalsOneOf("a", "b", "a", "c"));
        assertTrue(TypeUtils.equalsOneOf("a", "b", "c", "a"));
        assertTrue(TypeUtils.equalsOneOf("a", "b", "c", "a", "d"));
        assertTrue(TypeUtils.equalsOneOf("a", null, "a", "b", "c"));
        assertTrue(TypeUtils.equalsOneOf(null, "b", "c", "a", null));

        assertFalse(TypeUtils.equalsOneOf(null));
        assertFalse(TypeUtils.equalsOneOf("a"));
        assertFalse(TypeUtils.equalsOneOf("a", "b"));
        assertFalse(TypeUtils.equalsOneOf("a", "b", "c"));
        assertFalse(TypeUtils.equalsOneOf("a", "b", "c", "d"));
        assertFalse(TypeUtils.equalsOneOf("a", "b", "c", "d", "e"));

        assertFalse(TypeUtils.equalsOneOf(null, "a", "b", "c"));
    }

    @Test
    void isAllEmpty() {
        assertTrue(TypeUtils.isAllEmpty(""));
        assertTrue(TypeUtils.isAllEmpty(null, ""));
        assertTrue(TypeUtils.isAllEmpty("", null));
        assertTrue(TypeUtils.isAllEmpty(null, null, null));
        assertTrue(TypeUtils.isAllEmpty("", "", ""));

        assertFalse(TypeUtils.isAllEmpty(" ", " ", " "));
        assertFalse(TypeUtils.isAllEmpty("a", "", " "));
        assertFalse(TypeUtils.isAllEmpty("", "a", " "));
        assertFalse(TypeUtils.isAllEmpty("", "", "a"));
        assertFalse(TypeUtils.isAllEmpty("a", "b", ""));
        assertFalse(TypeUtils.isAllEmpty("a", "", "b"));
        assertFalse(TypeUtils.isAllEmpty("", "a", "b"));
        assertFalse(TypeUtils.isAllEmpty("a", "b", "c"));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void isNotEmpty() {
        assertTrue(TypeUtils.isNotEmpty("a"));
        assertTrue(TypeUtils.isNotEmpty(" "));
        assertFalse(TypeUtils.isNotEmpty(""));
        assertFalse(TypeUtils.isNotEmpty((String) null));
    }

    @Test
    void isAnyEmpty() {
        assertTrue(TypeUtils.isAnyEmpty(""));
        assertTrue(TypeUtils.isAnyEmpty((String) null));
        assertTrue(TypeUtils.isAnyEmpty(null, "", " "));
        assertTrue(TypeUtils.isAnyEmpty("", " ", null));
        assertTrue(TypeUtils.isAnyEmpty("", " ", " "));
        assertTrue(TypeUtils.isAnyEmpty(null, null, null));
        assertTrue(TypeUtils.isAnyEmpty("", "", ""));
        assertTrue(TypeUtils.isAnyEmpty("a", "", " "));
        assertTrue(TypeUtils.isAnyEmpty("", "a", " "));

        assertFalse(TypeUtils.isAnyEmpty(" ", " ", " "));
        assertFalse(TypeUtils.isAnyEmpty("a", "b", "c"));
        assertFalse(TypeUtils.isAnyEmpty("a"));
    }

    @Test
    void defaultIfTest() {
        assertEquals("foo", TypeUtils.defaultIfEmpty("foo", "bar"));
        assertEquals(" ", TypeUtils.defaultIfEmpty(" ", "bar"));
        assertEquals("bar", TypeUtils.defaultIfEmpty("", "bar"));
        assertEquals("bar", TypeUtils.defaultIfEmpty(null, "bar"));

        assertEquals("foo", TypeUtils.defaultIfBlank("foo", "bar"));
        assertEquals("bar", TypeUtils.defaultIfBlank(" ", "bar"));
        assertEquals("bar", TypeUtils.defaultIfBlank("", "bar"));
        assertEquals("bar", TypeUtils.defaultIfBlank(null, "bar"));
    }

    @Test
    void startsWithIgnoreCaseTest() {
        assertTrue(TypeUtils.startsWithIgnoreCase(null, null), "Both null should return true");
        assertFalse(TypeUtils.startsWithIgnoreCase(null, "a"), "Null string should not start with any non-null prefix");
        assertTrue(TypeUtils.startsWithIgnoreCase("", ""), "Both empty return true");
        assertFalse(TypeUtils.startsWithIgnoreCase("testing", null), "Any string should not start with null prefix");
        assertTrue(TypeUtils.startsWithIgnoreCase("testing", ""), "String 'testing' should start with prefix ''");
        assertTrue(TypeUtils.startsWithIgnoreCase("testing", "test"), "String 'testing' should start with prefix 'test'");
        assertTrue(TypeUtils.startsWithIgnoreCase("TESTING", "test"), "String 'TESTING' should start with prefix 'test' ignoring case");
        assertTrue(TypeUtils.startsWithIgnoreCase("Istanbul", "ist"), "Turkish dotless-i test: 'Istanbul' starts with ist");
    }

}
