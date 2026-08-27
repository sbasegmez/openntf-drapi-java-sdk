package org.openntf.drapi.internal.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class BearerTokenTest {

    @Test
    void testBearerTokenCreationNoExpire() {
        String tokenValue = "eyJhbGciOiJI{...}"; // Example JWT token value
        BearerToken token = new BearerToken(tokenValue, Map.of());
        assertFalse(token.isExpired(), "Token should not be expired when no expiration claim is present");
    }

    @Test
    void testBearerTokenCreationWithExpirePast() {
        String tokenValue = "eyJhbGciOiJI{...}"; // Example JWT token value
        BearerToken token = new BearerToken(tokenValue, Map.of("exp", System.currentTimeMillis() / 1000 - 120));
        assertTrue(token.isExpired(), "Token should be expired when expiration claim is in the past");
    }


    @Test
    void testBearerTokenCreationWithExpireFuture() {
        String tokenValue = "eyJhbGciOiJI{...}"; // Example JWT token value
        BearerToken token = new BearerToken(tokenValue, Map.of("exp", System.currentTimeMillis() / 1000 + 60));
        assertFalse(token.isExpired(), "Token should not be expired when expiration claim is in the future");
    }

    @Test
    void testBearerTokenCreationWithExpireFutureSkew() {
        String tokenValue = "eyJhbGciOiJI{...}"; // Example JWT token value
        BearerToken token = new BearerToken(tokenValue, Map.of("exp", System.currentTimeMillis() / 1000 + 40));
        assertFalse(token.isExpired(30), "Token should not be expired when expiration claim is in the future with skew");
    }

    @Test
    void testBearerTokenDoesNotLeak() {
        String tokenValue = "eyJhbGciOiJI"; // Example JWT token value
        BearerToken token = new BearerToken(tokenValue, Map.of());
        assertFalse(token.toString().contains(tokenValue), "Token value should not be present in toString() output");
    }

}

