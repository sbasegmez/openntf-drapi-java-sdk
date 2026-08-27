package org.openntf.drapi.http;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DrapiResponseTest {

    @Test
    @DisplayName("Headers should be case-insensitive")
    void testHeadersCaseInsensitive() {
        try(DrapiResponse response = new DrapiResponse(200, Map.of("Content-Type", List.of("application/json")), null)) {
            assertTrue(response.containsHeader("content-type"));
            assertTrue(response.containsHeader("CONTENT-TYPE"));
            assertEquals(List.of("application/json"), response.getHeaderValues("content-type").orElse(List.of()));
        }
    }

    @Test
    @DisplayName("Test containsHeader() method")
    void testContainsHeader() {
        try(DrapiResponse response = new DrapiResponse(200, Map.of("X-Custom-Header", List.of("value1")), null)) {
            assertFalse(response.containsHeader(null), "containsHeader should return false for null header name");
            assertTrue(response.containsHeader("X-Custom-Header"));
            assertFalse(response.containsHeader("Non-Existent-Header"));
        }
    }

    @Test
    @DisplayName("Test getHeaderValues() method")
    void testGetHeaderValues() {
        try(DrapiResponse response = new DrapiResponse(200, Map.of("X-Custom-Header", List.of("value1", "value2")), null)) {
            assertEquals(List.of("value1", "value2"), response.getHeaderValues("X-Custom-Header").orElse(List.of()));
            assertTrue(response.getHeaderValues("Non-Existent-Header").isEmpty(), "getHeaderValues should return empty Optional for non-existent header");
        }
    }

    @Test
    void testContainsHeaderValue() {
        try(DrapiResponse response = new DrapiResponse(200, Map.of("X-Custom-Header", List.of("value1", "value2")), null)) {
            assertTrue(response.containsHeaderValue("X-Custom-Header", "value1"));
            assertTrue(response.containsHeaderValue("X-Custom-Header", "value2"));
            assertFalse(response.containsHeaderValue("X-Custom-Header", "value3"));
            assertFalse(response.containsHeaderValue("Non-Existent-Header", "value1"));
            assertFalse(response.containsHeaderValue(null, "value1"), "containsHeaderValue should return false for null header name");
            assertFalse(response.containsHeaderValue("X-Custom-Header", null), "containsHeaderValue should return false for null header value");
        }
    }
}
