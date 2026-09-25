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
package org.openntf.drapi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openntf.drapi.auth.TokenSourceProvider;
import org.openntf.drapi.auth.builtin.FixedTokenSourceProvider;
import org.openntf.drapi.auth.builtin.PasswordTokenSourceProvider;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.http.HttpTransportProvider;

class DrapiBuilderTest {

    @Test
    @DisplayName("Test DrapiBuilder with null parameters")
    void testNullConfig() {
        DrapiConfig config = DrapiConfig.builder().baseUrl("https://example.com").build();
        TokenSourceProvider tokenSourceProvider = new PasswordTokenSourceProvider();

        assertThrows(NullPointerException.class, () -> Drapi.builder(null, tokenSourceProvider), "Expected NullPointerException for null config parameter");
        assertThrows(NullPointerException.class, () -> Drapi.builder(config, null), "Expected NullPointerException for null tokenSourceProvider parameter");
    }

    @Test
    @DisplayName("Test DrapiBuilder with valid parameters")
    void testValidConfig() {
        DrapiConfig config = DrapiConfig.builder()
                                        .baseUrl("https://example.com")
                                        .addExtraParam("auth.token", "test-token")
                                        .build();

        TokenSourceProvider tokenSourceProvider = new FixedTokenSourceProvider();

        assertDoesNotThrow(() -> Drapi.builder(config, tokenSourceProvider).build(), "Expected no exception for valid parameters");
    }
}
