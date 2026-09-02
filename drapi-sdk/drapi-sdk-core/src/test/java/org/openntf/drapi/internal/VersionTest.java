package org.openntf.drapi.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VersionTest {

    @Test
    @DisplayName("Test that the version is not null, not a placeholder, and not empty")
    void testVersion() {
        String version = Version.get();
        assertNotNull(version, "Version should not be null");
        assertFalse(version.startsWith("${"), "Version should not be a placeholder");
        assertFalse(version.isEmpty(), "Version should not be empty");
    }

}
