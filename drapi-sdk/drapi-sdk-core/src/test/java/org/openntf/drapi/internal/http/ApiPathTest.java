package org.openntf.drapi.internal.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiPathTest {

    @Test
    @DisplayName("Test that ApiPath.of() correctly constructs an ApiPath with given segments")
    void testApiPathOf() {
        ApiPath path = ApiPath.of("segment1")
                              .append("segment2");

        assertNotNull(path);
        assertEquals("/segment1/segment2", path.toString(), "The constructed path should be '/segment1/segment2'");
    }

    @Test
    @DisplayName("Test that ApiPath.root() returns the correct root path")
    void testApiPathRoot() {
        ApiPath path = ApiPath.root();

        assertNotNull(path);
        assertEquals("/api/v1", path.toString(), "The root path should be '/api/v1'");
    }

    @Test
    @DisplayName("Test that ApiPath handles leading and trailing slashes correctly")
    void testApiPathLeadingAndTrailingSlashes() {
        ApiPath path = ApiPath.of("/segment1/").append("/segment2/");

        assertEquals("/segment1/segment2", path.toString(), "Leading and trailing slashes should be normalized in the path'");
    }

    @Test
    @DisplayName("Test that ApiPath handles empty segments correctly")
    void testApiPathEmptySegments() {
        ApiPath path = ApiPath.of("").append("test").append("");

        assertEquals("/test", path.toString(), "Empty segments should be ignored in the path");

        path = ApiPath.of("/").append("test").append("//");

        assertEquals("/test", path.toString(), "Slashes and empty segments should be ignored in the path");
    }

    @Test
    @DisplayName("Test that segments are URL-encoded correctly")
    void testApiPathUrlEncoding() {
        ApiPath path = ApiPath.of("segment with spaces").append("üñîçødê");

        assertEquals("/segment+with+spaces/%C3%BC%C3%B1%C3%AE%C3%A7%C3%B8d%C3%AA", path.toString(), "Segments should be URL-encoded correctly");
    }

    @Test
    @DisplayName("Test that ApiPath handles root-relative paths correctly")
    void testApiPathRootRelative() {
        ApiPath path = ApiPath.root("test");

        assertEquals("/api/v1/test", path.toString(), "Root-relative paths should be appended correctly");
    }

}
