package org.openntf.drapi.util;

import java.util.Arrays;
import java.util.Collection;

public class TypeUtils {

    private TypeUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Check if a string is null or empty.
     * <p>
     * null -> true "" -> true " " -> false
     * <p>
     *
     * @param value the string to check
     * @return true if the string is null or blank
     */
    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    /**
     * Check if a string is null or blank.
     * <p>
     * null -> true "" -> true " " -> true
     * <p>
     *
     * @param value the string to check
     * @return true if the string is null or blank
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public static <T> boolean isEmpty(Collection<T> list) {
        return list == null || list.isEmpty();
    }

    public static <T> boolean isNotEmpty(Collection<T> list) {
        return !isEmpty(list);
    }

    public static boolean isEmpty(Object obj) {
        if (obj instanceof String str) {
            return isEmpty(str);
        } else if (obj instanceof Collection<?> col) {
            return isEmpty(col);
        } else {
            return obj == null;
        }
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    public static String requireNonEmpty(String value, String message) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static boolean isNumeric(String text) {
        return isNotEmpty(text) && text.trim().matches("^\\d+$");
    }

    public static boolean equalsOneOf(String strToFind, String... strArgs) {
        return Arrays.asList(strArgs)
                     .contains(strToFind);
    }

    public static boolean isAllEmpty(String... strArgs) {
        return Arrays.stream(strArgs)
                     .allMatch(TypeUtils::isEmpty);
    }

    public static boolean isAnyEmpty(String... strArgs) {
        return Arrays.stream(strArgs)
                     .anyMatch(TypeUtils::isEmpty);
    }

    public static <T> T defaultIfEmpty(T value, T defaultValue) {
        return isEmpty(value) ? defaultValue : value;
    }

    public static String firstNonEmpty(String... values) {
        for (String otherValue : values) {
            if (isNotEmpty(otherValue)) {
                return otherValue;
            }
        }

        return null;
    }

    public static String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    /**
     * Null-safe toString(). This also makes sure object arrays are correctly converted to a string.
     *
     * @param obj object to convert
     * @return string representation of the object using toString() or null if the object is null
     */
    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Object[]) {
            return Arrays.toString((Object[]) obj);
        }

        return obj.toString();
    }

}
