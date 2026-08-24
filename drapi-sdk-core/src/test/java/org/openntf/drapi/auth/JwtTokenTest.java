package org.openntf.drapi.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class JwtTokenTest {

    @Test
    void testJwtTokenCreationNoExpire() {
        String tokenValue = "eyJhbGciOiJI{...}"; // Example JWT token value
        JwtToken token = new JwtToken(tokenValue, Map.of());
        assertFalse(token.isExpired(), "Token should not be expired when no expiration claim is present");
    }

    @Test
    void testJwtTokenCreationWithExpirePast() {
        String tokenValue = "eyJhbGciOiJI{...}"; // Example JWT token value
        JwtToken token = new JwtToken(tokenValue, Map.of("exp", System.currentTimeMillis() / 1000 - 120));
        assertTrue(token.isExpired(), "Token should be expired when expiration claim is in the past");
    }


    @Test
    void testJwtTokenCreationWithExpireFuture() {
        String tokenValue = "eyJhbGciOiJI{...}"; // Example JWT token value
        JwtToken token = new JwtToken(tokenValue, Map.of("exp", System.currentTimeMillis() / 1000 + 60));
        assertFalse(token.isExpired(), "Token should not be expired when expiration claim is in the future");
    }

    @Test
    void testJwtTokenCreationWithExpireFutureSkew() {
        String tokenValue = "eyJhbGciOiJI{...}"; // Example JWT token value
        JwtToken token = new JwtToken(tokenValue, Map.of("exp", System.currentTimeMillis() / 1000 + 40));
        assertFalse(token.isExpired(30), "Token should not be expired when expiration claim is in the future with skew");
    }

    @Test
    void testJwtTokenDoesNotLeak() {
        String tokenValue = "eyJhbGciOiJI"; // Example JWT token value
        JwtToken token = new JwtToken(tokenValue, Map.of());
        assertFalse(token.toString().contains(tokenValue), "Token value should not be present in toString() output");
    }

}

