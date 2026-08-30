package org.openntf.drapi.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

public class TypeUtils {

    private TypeUtils() {
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

    /**
     * Check if a string is not null and not empty.
     *
     * @param value the string to check
     * @return true if the string is not null and not empty
     */
    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    /**
     * Check if a string is null or blank.
     * <p>
     * null -> true, "" -> true, " " -> true
     * <p>
     *
     * @param value the string to check
     * @return true if the string is null or blank
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Check if a string is not null and not blank.
     *
     * @param value the string to check
     * @return true if the string is not null and not blank
     */
    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    /**
     * Check if a collection is null or empty.
     *
     * @param list the collection to check
     * @return true if the collection is null or empty
     */
    public static <T> boolean isEmpty(Collection<T> list) {
        return list == null || list.isEmpty();
    }

    /**
     * Check if a collection is not null and not empty.
     *
     * @param list the collection to check
     * @return true if the collection is not null and not empty
     */
    public static <T> boolean isNotEmpty(Collection<T> list) {
        return !isEmpty(list);
    }

    /**
     * Check if an object is null or empty. Supports String, Collection, and Object[] types.
     *
     * @param obj the object to check
     * @return true if the object is null or empty
     */
    public static boolean isEmpty(Object obj) {
        if (obj instanceof String str) {
            return isEmpty(str);
        } else if (obj instanceof Collection<?> col) {
            return isEmpty(col);
        } else if (obj instanceof Object[] array) {
            return array.length == 0;
        } else {
            return obj == null;
        }
    }

    /**
     * Check if an object is not null and not empty. Supports String and Collection types.
     *
     * @param obj the object to check
     * @return true if the object is not null and not empty
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * Require that a string is not null and not empty.
     *
     * @param value   the string to check
     * @param message the exception message to use if the check fails
     * @return the string if it is not null and not empty
     * @throws IllegalArgumentException if the string is null or empty
     */
    public static String requireNonEmpty(String value, String message) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    /**
     * Require that a string is not null and not blank.
     *
     * @param value   the string to check
     * @param message the exception message to use if the check fails
     * @return the string if it is not null and not blank
     * @throws IllegalArgumentException if the string is null or blank
     */
    public static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    /**
     * Check if a string is numeric.
     *
     * @param text the string to check
     * @return true if the string is numeric
     */
    public static boolean isNumeric(String text) {
        return isNotEmpty(text) && text.trim().matches("^\\d+$");
    }

    /**
     * Check if a string is equal to any of the provided strings.
     *
     * @param strToFind the string to find
     * @param strArgs   the strings to check against
     * @return true if the string is equal to any of the provided strings
     */
    public static boolean equalsOneOf(String strToFind, String... strArgs) {
        return Arrays.asList(strArgs)
                     .contains(strToFind);
    }

    /**
     * Check if all provided strings are empty.
     *
     * @param strArgs the strings to check
     * @return true if all provided strings are empty
     */
    public static boolean isAllEmpty(String... strArgs) {
        return Arrays.stream(strArgs)
                     .allMatch(TypeUtils::isEmpty);
    }

    /**
     * Check if all provided strings are non-empty.
     *
     * @param strArgs the strings to check
     * @return true if all provided strings are non-empty
     */
    public static boolean isAllNonEmpty(String... strArgs) {
        return Arrays.stream(strArgs)
                     .allMatch(TypeUtils::isNotEmpty);
    }

    /**
     * Check if any of the provided strings are empty.
     *
     * @param strArgs the strings to check
     * @return true if any of the provided strings are empty
     */
    public static boolean isAnyEmpty(String... strArgs) {
        return Arrays.stream(strArgs)
                     .anyMatch(TypeUtils::isEmpty);
    }

    /**
     * Return the default value if the provided value is empty.
     *
     * @param value        the value to check
     * @param defaultValue the default value to return if the value is empty
     * @param <T>          the type of the value
     * @return the value if it is not empty, otherwise the default value
     */
    public static <T> T defaultIfEmpty(T value, T defaultValue) {
        return isEmpty(value) ? defaultValue : value;
    }

    /**
     * Return the default value from the supplier if the provided value is empty.
     *
     * @param value                the value to check
     * @param defaultValueSupplier the supplier of the default value to return if the value is empty
     * @param <T>                  the type of the value
     * @return the value if it is not empty, otherwise the default value from the supplier
     */
    public static <T> T defaultIfEmpty(T value, Supplier<T> defaultValueSupplier) {
        return isEmpty(value) ? defaultValueSupplier.get() : value;
    }

    /**
     * Returns an optional containing the first non-empty string from the provided values. If all values are empty, the optional will be
     * empty.
     *
     * @param values the strings to check
     * @return an optional containing the first non-empty string, or an empty optional if all are empty
     */
    public static Optional<String> firstNonEmpty(String... values) {
        for (String otherValue : values) {
            if (isNotEmpty(otherValue)) {
                return Optional.of(otherValue);
            }
        }

        return Optional.empty();
    }

    /**
     * Return the default value if the provided value is blank.
     *
     * @param value        the value to check
     * @param defaultValue the default value to return if the value is blank
     * @return the value if it is not blank, otherwise the default value
     */
    public static String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    /**
     * Return the default value from the supplier if the provided value is blank.
     *
     * @param value                the value to check
     * @param defaultValueSupplier the supplier of the default value to return if the value is blank
     * @return the value if it is not blank, otherwise the default value from the supplier
     */
    public static String defaultIfBlank(String value, Supplier<String> defaultValueSupplier) {
        return isBlank(value) ? defaultValueSupplier.get() : value;
    }

    /**
     * Check if a string starts with a given prefix, ignoring case.
     *
     * @param key    the string to check.
     * @param prefix the prefix to check for.
     * @return true if the string starts with the prefix, ignoring case; false otherwise
     */
    public static boolean startsWithIgnoreCase(String key, String prefix) {
        if (null == key) {
            return null == prefix;
        }

        if (key.isEmpty()) {
            return prefix.isEmpty();
        }

        if (prefix == null) {
            return false;
        }

        return key.toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase(Locale.ENGLISH));
    }
}
