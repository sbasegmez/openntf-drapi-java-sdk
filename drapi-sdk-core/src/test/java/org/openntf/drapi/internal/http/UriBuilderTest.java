package org.openntf.drapi.internal.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UriBuilderTest {


    /**
     * Convenience methods to build a URI
     */
    private UriBuilder builder(String baseUrl) {
        return UriBuilder.startWith(baseUrl);
    }

    private UriBuilder builder(String baseUrl, String... paths) {
        var builder = builder(baseUrl);

        for (String path : paths) {
            builder.appendPath(path);
        }

        return builder;
    }

    private String buildWith(String baseUrl) {
        return builder(baseUrl).build().toString();
    }

    private String buildWith(String baseUrl, String... paths) {
        return builder(baseUrl, paths).build().toString();
    }

    @Test
    @DisplayName("Test UriBuilder with valid base URLs")
    void testUriBuilderWithValidBaseUrls() {

        assertEquals("https://example.com", buildWith("https://example.com"), "https scheme should be valid");
        assertEquals("http://example.com", buildWith("http://example.com"), "http scheme should be valid");
        assertEquals("http://example.com", buildWith("http://example.com "), "Configuration with trailing space should be valid");
        assertEquals("http://example.com", buildWith("http://example.com/"), "Trailing slash should be removed");
        assertEquals("http://example.com", buildWith("http://example.com/"), "Trailing slash should be removed");
        assertEquals("http://example.com:8118", buildWith("http://example.com:8118"), "Port should be preserved");
        assertEquals("http://[::1]:8080", buildWith("http://[::1]:8080"), "IPv6 host should be valid");
        assertEquals("https://sub.api.domain.com", buildWith("https://sub.api.domain.com"), "Subdomain should be valid");
    }


    @Test
    @DisplayName("Test UriBuilder with invalid base URLs")
    void testUriBuilderWithInvalidBaseUrls() {
        assertThrows(IllegalArgumentException.class, () -> buildWith(null), "Null should throw exception");
        assertThrows(IllegalArgumentException.class, () -> buildWith(""), "Empty string should throw exception");
        assertThrows(IllegalArgumentException.class, () -> buildWith(" "), "Blank string should throw exception");
        assertThrows(IllegalArgumentException.class, () -> buildWith("api.example.com"), "Missing scheme should throw exception");
        assertThrows(IllegalArgumentException.class, () -> buildWith("https://"), "Missing host should throw exception");
        assertThrows(IllegalArgumentException.class, () -> buildWith("://api.example.com"), "Invalid URL should throw exception");
        assertThrows(IllegalArgumentException.class, () -> buildWith("http://api example.com"), "Invalid URL should throw exception");
        assertThrows(IllegalArgumentException.class, () -> buildWith("ftp://api.example.com"), "Non-http/https URL should throw exception");
        assertThrows(IllegalArgumentException.class, () -> buildWith("http://api.example.com:ABC"), "Invalid port should throw exception");
        assertThrows(IllegalArgumentException.class, () -> buildWith("https://api.example.com:%38%30"), "Invalid port should throw exception");
        assertThrows(IllegalArgumentException.class, () -> buildWith("http://api.example.com/app"), "Base URL must not contain a path");
        assertThrows(IllegalArgumentException.class, () -> buildWith("http://api.example.com/app/.."), "Base URL must not contain a path, even if it resolves to root");
        assertThrows(IllegalArgumentException.class, () -> buildWith("http://user:pass@api.example.com"), "User info must not be included in the base URL");
        assertThrows(IllegalArgumentException.class, () -> buildWith("http://api.example.com//"), "Double slash must not be at the end of a path");
        assertThrows(IllegalArgumentException.class, () -> buildWith("http://api.example.com?query=value"), "Base URL must not contain a query string");
        assertThrows(IllegalArgumentException.class, () -> buildWith("http://api.example.com#section"), "Base URL must not contain a fragment");
        assertThrows(IllegalArgumentException.class, () -> buildWith("http://api.example.com?query=value#section"), "Base URL must not contain a query string and fragment");
    }

    @Test
    @DisplayName("Test UriBuilder paths are appended correctly")
    void testUriBuilderAppendSinglePath() {
        String result = buildWith("https://example.com", "api");
        assertEquals("https://example.com/api", result, "Paths should be appended correctly");

        result = buildWith("https://example.com:9900", "api");
        assertEquals("https://example.com:9900/api", result, "Port should be preserved in the base URL");

        result = buildWith("https://example.com", "api", "scopes");
        assertEquals("https://example.com/api/scopes", result, "Paths should be appended correctly");

        result = buildWith("https://example.com", "v1/api", "scopes");
        assertEquals("https://example.com/v1/api/scopes", result, "Multi-segment paths should be appended correctly");

    }

    @Test
    @DisplayName("Test UriBuilder paths are normalized correctly")
    void testUriBuilderAppendSinglePathNormalized() {
        String result = buildWith("https://example.com/", "/api/");
        assertEquals("https://example.com/api", result, "Paths with leading and trailing slashes should be normalized and appended correctly");

        result = buildWith("https://example.com", "api/");
        assertEquals("https://example.com/api", result, "Paths with trailing slashes should be normalized and appended correctly");

        result = buildWith("https://example.com", "/api");
        assertEquals("https://example.com/api", result, "Paths with leading slashes should be normalized and appended correctly");

        result = buildWith("https://example.com", " api/ ");
        assertEquals("https://example.com/api", result, "Paths should be trimmed and appended correctly");

        result = buildWith("https://example.com", "/v1/api/", "/scopes");
        assertEquals("https://example.com/v1/api/scopes", result, "Multi-segment paths with leading and trailing slashes should be normalized and appended correctly");

        result = buildWith("https://example.com", "/test path");
        assertEquals("https://example.com/test+path", result, "Paths with spaces should be URL-encoded correctly");

    }

    @Test
    @DisplayName("Test UriBuilder with invalid paths")
    void testUriBuilderWithInvalidPaths() {
        String result = buildWith("https://example.com", (String) null);
        assertEquals("https://example.com", result, "Null path should be ignored and not affect the URL");

        result = buildWith("https://example.com", "");
        assertEquals("https://example.com", result, "Empty path should be ignored and not affect the URL");

        result = buildWith("https://example.com", " ", "/scopes");
        assertEquals("https://example.com/scopes", result, "Blank path should be ignored and not affect the URL");

        result = buildWith("https://example.com", "/");
        assertEquals("https://example.com", result, "Blank multi-segment paths should be ignored and not affect the URL");

    }

    @Test
    @DisplayName("Test UriBuilder with valid query parameters")
    void testUriBuilderWithValidQueryParams() {
        String result = builder("https://example.com", "api")
            .appendQueryParam("key1", "value1")
            .appendQueryParam("key2", "value2")
            .appendQueryParam("key3", "") // This should be ignored
            .appendQueryParam("key4", null) // This should be ignored
            .appendQueryParam(null, null) // This should be ignored
            .build()
            .toString();

        assertEquals("https://example.com/api?key1=value1&key2=value2", result, "Query parameters should be appended correctly");
    }

    @Test
    @DisplayName("Test UriBuilder allows duplicate query parameters")
    void testUriBuilderAllowsDuplicateQueryParams() {
        String result = builder("https://example.com", "api")
            .appendQueryParam("key1", "value1")
            .appendQueryParam("key1", "value2")
            .build()
            .toString();

        assertEquals("https://example.com/api?key1=value1&key1=value2", result, "Duplicate query parameters should be appended correctly");
    }

    @Test
    @DisplayName("Test UriBuilder with query parameters that need encoding")
    void testUriBuilderWithQueryParamsThatNeedEncoding() {
        String result = builder("https://example.com", "api")
            .appendQueryParam("key+1", "value 1")
            .appendQueryParam("key&2", "value&2")
            .appendQueryParam("pwd?", "café")
            .build()
            .toString();

        assertEquals("https://example.com/api?key%2B1=value+1&key%262=value%262&pwd%3F=caf%C3%A9", result, "Query parameters should be URL-encoded correctly");
    }

    @Test
    @DisplayName("Test UriBuilder to ensure order of paths and query parameters is handled correctly")
    void testUriBuilderWithOrderOfPathsAndQueryParams() {
        String result = builder("https://example.com", "api")
            .appendQueryParam("key1", "value1")
            .appendPath("scopes")
            .appendQueryParam("key2", "value2")
            .build()
            .toString();
        assertEquals("https://example.com/api/scopes?key1=value1&key2=value2", result, "Order of paths and query parameters should be handled correctly");
    }

}
