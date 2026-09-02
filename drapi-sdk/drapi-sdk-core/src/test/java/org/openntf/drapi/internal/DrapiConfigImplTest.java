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
                                        .basic("username", "password")
                                        .build();

        assertNotNull(config, "Config should not be null");
        assertEquals("https://example.com:8089/", config.baseUrl().toString(), "Base URL should match");
        assertEquals(DrapiConfig.AuthType.BASIC, config.authType(), "Auth type should be BASIC");
        assertEquals("username", config.username(), "Username should match");
        assertEquals("password", config.password(), "Password should match");

        assertNotNull(config.userAgent(), "User agent should not be null");
        assertTrue(config.connectTimeoutSecs() > 0, "Connect timeout should be greater than 0");
        assertTrue(config.requestTimeoutSecs() > 0, "Request timeout should be greater than 0");
    }

    @Test
    @DisplayName("Custom user agent should not append version tag")
    void testCustomUserAgentWithVersion() {
        DrapiConfig config = DrapiConfig.builder()
                                        .baseUrl("https://example.com")
                                        .basic("username", "password")
                                        .userAgent("MyApp/1.8.8569")
                                        .build();

        assertEquals("MyApp/1.8.8569", config.userAgent(), "User agent should not append version tag when customized");
    }

    @Test
    @DisplayName("Test DrapiConfigImpl with TOKEN auth")
    void testTokenAuthConfig() {
        DrapiConfig config = DrapiConfig.builder()
                                        .baseUrl("https://example.com")
                                        .token("my-token")
                                        .build();

        assertEquals(DrapiConfig.AuthType.TOKEN, config.authType(), "Auth type should be TOKEN");
        assertEquals("my-token", config.token(), "Token should match");
    }

    @Test
    @DisplayName("Test DrapiConfigImpl with OAUTH auth")
    void testOAuthAuthConfig() {
        DrapiConfig config = DrapiConfig.builder()
                                        .baseUrl("https://example.com")
                                        .oauth("app-id", "app-secret")
                                        .build();

        assertEquals(DrapiConfig.AuthType.OAUTH, config.authType(), "Auth type should be OAUTH");
        assertEquals("app-id", config.appId(), "App ID should match");
        assertEquals("app-secret", config.appSecret(), "App Secret should match");
    }

    @Test
    @DisplayName("Test invalid URI")
    void testInvaliURI() {
        assertThrows(NullPointerException.class, () -> DrapiConfig.builder()
                                                                  .baseUrl((String) null)
                                                                  .build(),
                     "Null URL should generate NullPointerException");

        assertThrows(IllegalArgumentException.class, () -> DrapiConfig.builder()
                                                                  .baseUrl("-")
                                                                  .build(),
                     "Invalid URL should generate IllegalArgumentException");
    }

    @Test
    @DisplayName("Test DrapiConfigImpl with invalid config")
    void testInvalidConfig() {
        assertThrows(IllegalArgumentException.class, () -> DrapiConfig.builder()
                                                                      .build(), "Expected IllegalArgumentException for missing base URL");

        assertThrows(IllegalArgumentException.class, () -> DrapiConfig.builder()
                                                                      .baseUrl("https://example.com")
                                                                      .build(), "Expected IllegalArgumentException for not providing any authentication method");

        assertThrows(IllegalArgumentException.class, () -> DrapiConfig.builder()
                                                                      .baseUrl("https://example.com")
                                                                      .basic(null, "password")
                                                                      .build(), "Expected IllegalArgumentException for missing username");

        assertThrows(IllegalArgumentException.class, () -> DrapiConfig.builder()
                                                                      .baseUrl("https://example.com")
                                                                      .basic("username", "")
                                                                      .build(), "Expected IllegalArgumentException for blank password");

        assertThrows(IllegalArgumentException.class, () -> DrapiConfig.builder()
                                                                      .baseUrl("https://example.com")
                                                                      .oauth("app-id", "")
                                                                      .build(), "Expected IllegalArgumentException for blank app secret");

    }

}
