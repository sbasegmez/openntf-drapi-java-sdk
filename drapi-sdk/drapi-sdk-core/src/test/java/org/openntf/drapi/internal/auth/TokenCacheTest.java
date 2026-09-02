/*
 * Copyright (c) 2026 Serdar Basegmez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.drapi.internal.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openntf.drapi.internal.test.TestUtils;

class TokenCacheTest {

    AtomicInteger tokenCounter;
    AtomicReference<BearerToken> currentToken;
    TokenCache tokenCache;

    @BeforeEach
    void setUp() {
        tokenCounter = new AtomicInteger(0);
        currentToken = new AtomicReference<>();

        // Create a TokenCache with a supplier that returns a new BearerToken
        tokenCache = new TokenCache(() -> {
            tokenCounter.incrementAndGet();
            BearerToken token = new BearerToken("test-token-" + tokenCounter.get(), Map.of());
            currentToken.set(token);
            return token;
        });
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("Test TokenCache get method")
    void testTokenCacheGet() {
        // Get the token
        BearerToken token = tokenCache.get();

        assertNotNull(token, "The token should be present after the first get call.");
        assertSame(currentToken.get(), token, "The token value should match the expected value.");

        tokenCache.get(); // Call get again to ensure the token is cached

        assertSame(currentToken.get(), token, "The token value should still match the expected value.");
        assertEquals(1, tokenCounter.get(), "The token supplier should have been called only once.");
    }

    @Test
    @DisplayName("Test TokenCache get method with Expired Token")
    void testTokenCacheGetWithExpiredToken() {
        BearerToken expiredToken = new BearerToken("test-token", Map.of("exp",
                                                                        System.currentTimeMillis() / 1000 - 10)); // Expired token
        BearerToken newToken = new BearerToken("test-token", Map.of()); // New token

        // Create a TokenCache with a supplier that returns a new BearerToken
        TokenCache expiringCache = new TokenCache(() -> {
            if (tokenCounter.incrementAndGet() == 1) {
                return expiredToken; // Return expired token on first call
            }
            return newToken;
        });

        // Get the token
        BearerToken token = expiringCache.get();

        assertNotNull(token, "The token should be present after the first get call.");
        assertSame(expiredToken, token, "The token value should match the expected value.");

        BearerToken token2 = expiringCache.get(); // Call get again to ensure the token is cached

        assertNotNull(token2, "The token should be present after the second get call.");
        assertSame(newToken, token2, "The token value should match the new token");
        assertEquals(2, tokenCounter.get(), "The token supplier should have been called twice.");
    }

    @Test
    @DisplayName("Test TokenCache invalidate method")
    void testTokenCacheInvalidate() {
        // Get the token
        BearerToken token = tokenCache.get();

        assertNotNull(token, "The token should be present after the first get call.");

        BearerToken lastToken = currentToken.get();

        assertSame(lastToken, token, "The token value should match the expected value.");

        // Invalidate the token
        tokenCache.invalidate(lastToken);

        BearerToken tokenAfterInvalidate = tokenCache.get();

        assertNotNull(tokenAfterInvalidate, "The token should be present after invalidation.");
        assertNotSame(lastToken, tokenAfterInvalidate, "The token value should not match the invalidated token");
        assertEquals(2, tokenCounter.get(), "The token supplier should have been called twice.");
    }

    @Test
    @DisplayName("Test TokenCache invalidate method with non-matching token")
    void testTokenCacheInvalidateNonMatchingToken() {
        // Get a token
        BearerToken aToken = tokenCache.get();

        // Invalidate a non-matching token
        BearerToken nonMatchingToken = new BearerToken("non-matching-token", Map.of());
        tokenCache.invalidate(nonMatchingToken);

        assertSame(aToken, currentToken.get(), "The token should not be invalidated when a non-matching token is provided.");

        BearerToken tokenAfterInvalidate = tokenCache.get();

        assertNotNull(tokenAfterInvalidate, "The token should be present after invalidation.");
        assertSame(aToken, tokenAfterInvalidate, "The token value should match the original token");
        assertEquals(1, tokenCounter.get(), "The token supplier should have been called once.");
    }

    @Test
    @DisplayName("Null invalidation test")
    void testTokenCacheInvalidateNull() {
        // Call invalidate with null
        assertDoesNotThrow(() -> tokenCache.invalidate(null), "Invalidating with null without a token should not throw an exception.");

        // Make sure there is a token in the cache
        tokenCache.get();

        // Call invalidate with null
        assertDoesNotThrow(() -> tokenCache.invalidate(null), "Invalidating with null with a token should not throw an exception.");
    }

    @Test
    @DisplayName("Invalidation should not use equals() for token comparison")
    void testTokenCacheInvalidateDoesNotUseEquals() {
        // Make sure there is a token in the cache
        BearerToken token = tokenCache.get();

        // Create a new token with the same bearer but different instance. It should not be equal to the cached token.
        assertFalse(tokenCache.invalidate(new BearerToken(token.bearer(), token.claims())));
    }

    @Test
    @DisplayName("Test Token Supplier throws an exception")
    void testTokenSupplierThrowsException() {
        // Create a TokenCache with a supplier that throws an exception
        TokenCache exceptionCache = new TokenCache(() -> {
            throw new RuntimeException("Token acquisition failed");
        });

        assertThrows(RuntimeException.class, exceptionCache::get, "An exception should be thrown when the token supplier fails.");
    }

    @Test
    @DisplayName("Concurrent access test")
    void testConcurrentAccess() {
        // Create a TokenCache with a supplier that returns a new BearerToken
        TokenCache slowCache = new TokenCache(() -> {
            tokenCounter.incrementAndGet();
            try {
                Thread.sleep(100); // Simulate a delay in token acquisition
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new BearerToken("test-token-" + tokenCounter.get(), Map.of());
        });

        Set<BearerToken> acquiredTokens = ConcurrentHashMap.newKeySet();

        Runnable task = () -> {
            BearerToken token = slowCache.get();
            assertNotNull(token, "The token should be present in concurrent access.");
            acquiredTokens.add(token);
        };

        TestUtils.runMultipleThreadsAtTheSameTime(10, task);

        assertEquals(1, acquiredTokens.size(), "All threads should have received the same token instance.");
        assertEquals(1, tokenCounter.get(), "The token supplier should have been called only once in concurrent access.");
    }

}
