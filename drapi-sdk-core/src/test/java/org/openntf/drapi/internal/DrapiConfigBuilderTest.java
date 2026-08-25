package org.openntf.drapi.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openntf.drapi.DrapiConfig;

class DrapiConfigBuilderTest {

    @Test
    @DisplayName("Test duration, instead of seconds, for connectTimeout and requestTimeout")
    void testDurationTimeouts() {
        DrapiConfig config = DrapiConfig.builder()
                                        .baseUrl("https://example.com")
                                        .basic("username", "password")
                                        .connectTimeout(java.time.Duration.ofSeconds(10))
                                        .requestTimeout(java.time.Duration.ofMinutes(1))
                                        .build();

        assertEquals(10, config.connectTimeoutSecs(), "Connect timeout should be 10 seconds");
        assertEquals(60, config.requestTimeoutSecs(), "Request timeout should be 60 seconds");

    }

}
