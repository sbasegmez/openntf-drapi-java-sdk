package org.openntf.drapi.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openntf.drapi.DrapiClient;

class DrapiClientBuilderTest {

    @Test
    @DisplayName("Test DrapiClientBuilder with null config")
    void testNullConfig() {
        assertThrows(NullPointerException.class, () -> DrapiClient.builder(null), "Expected NullPointerException for null config");
    }

}
