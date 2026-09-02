package org.openntf.drapi.http;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DrapiResponseTest {

    @Test
    @DisplayName("Headers should be case-insensitive")
    void testHeadersCaseInsensitive() {
        try (DrapiResponse response = new DrapiResponse(200, Map.of("Content-Type", List.of("application/json")), null)) {
            assertTrue(response.containsHeader("content-type"));
            assertTrue(response.containsHeader("CONTENT-TYPE"));
            assertEquals(List.of("application/json"), response.getHeaderValues("content-type").orElse(List.of()));
        }
    }

    @Test
    @DisplayName("Test getHeaderValues() method")
    void testGetHeaderValues() {
        try (DrapiResponse response = new DrapiResponse(200, Map.of("X-Custom-Header", List.of("value1", "value2")), null)) {
            assertEquals(List.of("value1", "value2"), response.getHeaderValues("X-Custom-Header").orElse(List.of()));
            assertTrue(response.getHeaderValues("Non-Existent-Header")
                               .isEmpty(), "getHeaderValues should return empty Optional for non-existent header");
        }
    }

    @Test
    @DisplayName("Test containsHeader() method")
    void testContainsHeader() {
        try (DrapiResponse response = new DrapiResponse(200, Map.of("X-Custom-Header", List.of("value1", "value2")), null)) {
            assertFalse(response.containsHeader(null), "containsHeader should return false for null header name");
            assertTrue(response.containsHeader("X-Custom-Header"), "containsHeader should return true for existing header");
            assertFalse(response.containsHeader("Non-Existent-Header"), "containsHeader should return false for non-existent header");

            assertTrue(response.containsHeader("X-Custom-Header", "value1"), "containsHeader should return true for existing header and value");
            assertTrue(response.containsHeader("X-Custom-Header", "value2"), "containsHeader should detect multiple header values");
            assertFalse(response.containsHeader("X-Custom-Header", "value3"), "containsHeader should return false for existing header but non-existent value");
            assertFalse(response.containsHeader("Non-Existent-Header", "value1"), "containsHeader should return false for non-existent header and any value");
            assertFalse(response.containsHeader(null, "value1"), "containsHeader should return false for null header name");
            assertFalse(response.containsHeader("X-Custom-Header", null), "containsHeader should return false for null header value");
        }
    }
}
