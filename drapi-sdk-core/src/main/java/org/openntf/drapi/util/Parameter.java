package org.openntf.drapi.util;

/**
 * Represents a key-value pair parameter, typically used for query parameters in HTTP requests.
 *
 * @param key   the parameter key
 * @param value the parameter value
 */
public record Parameter(String key, String value) {

}
