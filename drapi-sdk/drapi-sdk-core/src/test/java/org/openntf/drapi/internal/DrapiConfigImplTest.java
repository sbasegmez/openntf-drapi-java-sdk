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
package org.openntf.drapi.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openntf.drapi.DrapiConfig;

class DrapiConfigImplTest {

    @Test
    @DisplayName("Test DrapiConfigImpl with BASIC auth")
    void testBasicAuthConfig() {
        DrapiConfig config = DrapiConfig.builder()
                                        .baseUrl("https://example.com:8089/")
                                        .addExtraParam("username", "username")
                                        .addExtraParam("password", "password")
                                        .build();

        assertNotNull(config, "Config should not be null");
        assertEquals("https://example.com:8089/", config.baseUrl().toString(), "Base URL should match");
        assertEquals("username", config.get("username", String.class).orElse(null), "Username should match");
        assertEquals("password", config.get("password", String.class).orElse(null), "Password should match");

        assertNotNull(config.userAgent(), "User agent should not be null");
        assertTrue(config.connectTimeoutSecs() > 0, "Connect timeout should be greater than 0");
        assertTrue(config.requestTimeoutSecs() > 0, "Request timeout should be greater than 0");
    }

    @Test
    @DisplayName("Custom user agent should not append version tag")
    void testCustomUserAgentWithVersion() {
        DrapiConfig config = DrapiConfig.builder()
                                        .baseUrl("https://example.com")
                                        .userAgent("MyApp/1.8.8569")
                                        .build();

        assertEquals("MyApp/1.8.8569", config.userAgent(), "User agent should not append version tag when customized");
    }

    @Test
    @DisplayName("Test invalid URI")
    void testInvalidURI() {
        assertThrows(NullPointerException.class, () -> DrapiConfig.builder()
                                                                  .baseUrl((String) null)
                                                                  .build(),
                     "Null URL should generate NullPointerException");

        assertThrows(IllegalArgumentException.class, () -> DrapiConfig.builder()
                                                                  .baseUrl("-")
                                                                  .build(),
                     "Invalid URL should generate IllegalArgumentException");
    }

}
