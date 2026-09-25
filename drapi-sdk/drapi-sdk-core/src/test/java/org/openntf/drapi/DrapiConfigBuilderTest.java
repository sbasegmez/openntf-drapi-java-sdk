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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openntf.drapi.internal.DrapiConfigImpl;
import org.openntf.drapi.util.ConfigKey;

class DrapiConfigBuilderTest {

    @Test
    @DisplayName("Test duration, instead of seconds, for connectTimeout and requestTimeout")
    void testDurationTimeouts() {
        DrapiConfig config = DrapiConfig.builder()
                                        .baseUrl("https://example.com")
                                        .connectTimeout(java.time.Duration.ofSeconds(10))
                                        .requestTimeout(java.time.Duration.ofMinutes(1))
                                        .build();

        assertEquals("https://example.com", config.baseUrl().toString(), "Base URL should match the builder");
        assertEquals(10, config.connectTimeoutSecs(), "Connect timeout should be 10 seconds");
        assertEquals(60, config.requestTimeoutSecs(), "Request timeout should be 60 seconds");
    }

    @Test
    @DisplayName("Test loading configuration from properties file")
    void testLoadFromPropertiesFile() {
        DrapiConfig config = DrapiConfig.builder()
                                        .applyResourceFile("config/example.properties")
                                        .build();

        assertEquals("https://api.example.com", config.baseUrl().toString(), "Base URL should match the properties file");

        assertEquals("$DATA", config.get("auth.scope", String.class).orElse(null), "Auth scope should match the properties file");
        assertEquals("your_username", config.get("auth.username", String.class)
                                            .orElse(null), "Username should match the properties file");
        assertEquals("your_username", config.get("auth.USERNAME", String.class)
                                            .orElse(null), "Keys should match even if the case is different");
        assertEquals("your_password", config.get("auth.password", String.class)
                                            .orElse(null), "Password should match the properties file");
        assertTrue(config.userAgent().startsWith("your_user_agent"), "User agent should match the properties file");
        assertEquals(13, config.connectTimeoutSecs(), "Connect timeout should match the properties file");
        assertEquals(DrapiConfigImpl.DEFAULT_REQUEST_TIMEOUT_SECS, config.requestTimeoutSecs(), "Request timeout should be ignored from invalid property");
    }

    @Test
    @DisplayName("Test loading configuration from map with invalid values")
    void testLoadFromMapWithInvalidValues() {
        DrapiConfig config = DrapiConfig.builder()
                                        .applyMap(Map.of(
                                            "baseUrl", "https://api.example.com",
                                            "username", "your_username",
                                            "password", "your_password",
                                            "useragent", " ", // Blank value
                                            "connectTimeoutSecs", "-5" // Invalid value
                                        ))
                                        .build();

        assertEquals("https://api.example.com", config.baseUrl().toString(), "Base URL should match the map");
        assertEquals("your_username", config.get("username", String.class).orElse(null), "Username should match the map");
        assertEquals("your_password", config.get("password", String.class).orElse(null), "Password should match the map");
        assertTrue(config.userAgent()
                         .startsWith(DrapiConfigImpl.DEFAULT_USER_AGENT), "User agent should fallback to default due to blank value");
        assertEquals(DrapiConfigImpl.DEFAULT_CONNECT_TIMEOUT_SECS, config.connectTimeoutSecs(), "Connect timeout should fallback to default due to invalid value");
        assertEquals(DrapiConfigImpl.DEFAULT_REQUEST_TIMEOUT_SECS, config.requestTimeoutSecs(), "Request timeout should fallback to default due to invalid value");
    }

    @Test
    @DisplayName("Test loading configuration from map with prefix")
    void testLoadFromMapWithOAuth() {
        var config = DrapiConfig.builder()
                                .doApplyEnvironmentVariables(Map.of(
                                    "DRAPI_BASEURL", "https://api.example.com",
                                    "DRAPI_AUTHSCOPE", "$DATA",
                                    "DRAPI_APPID", "your_ap_id",
                                    "DRAPI_APP_SECRET1", "your_secret1",
                                    "DRAPI_APP.SECRET2", "your_secret2",
                                    "DRAPI_USERAGENT", "MyApp"
                                ), "DRAPI_")
                                .build();

        assertEquals("https://api.example.com", config.baseUrl().toString(), "Base URL should match the map");
        assertEquals("$DATA", config.get("AUTHSCOPE", String.class).orElse(null), "Auth scope should match the map");
        assertEquals("your_ap_id", config.get("APPID", String.class).orElse(null), "App ID should match the map");
        assertEquals("your_secret1", config.get("APP.SECRET1", String.class)
                                           .orElse(null), "Underscores should be converted to dots in keys for env variables");
        assertEquals("your_secret2", config.get("APP.SECRET2", String.class)
                                           .orElse(null), "Dots should stay in keys for env variables");
        assertEquals("MyApp", config.userAgent(), "User agent should match the map");
    }

    @Test
    @DisplayName("Test setting and getting arbitrary parameters")
    void testSetAndGetArbitraryParameters() {
        var intKey1 = ConfigKey.of("param1", Integer.class);
        var strKey1 = ConfigKey.of("param1", String.class);

        DrapiConfig config = DrapiConfig.builder()
                                        .baseUrl("https://example.com")
                                        .addExtraParam(intKey1, 42)
                                        .addExtraParam("anotherParam", "value")
                                        .addExtraParam("param3", null)
                                        .build();

        assertEquals(42, config.get(intKey1).orElse(null), "Parameter should match the set value");
        assertNotEquals("42", config.get(strKey1).orElse(null), "Parameter types should not be lenient");
        assertEquals("value", config.get("anotherParam", String.class).orElse(null), "Another parameter should match the set value");
        assertTrue(config.get("param3", String.class).isEmpty(), "Null parameter should return empty Optional");
        assertTrue(config.get("nonExistentParam", String.class).isEmpty(), "Non-existent parameter should return empty Optional");
    }

    @EnabledIfEnvironmentVariable(named = "DRAPI_BASEURL", matches = ".*")
    @EnabledIfEnvironmentVariable(named = "DRAPI_AUTH_TOKEN", matches = ".*")
    @Test
    @DisplayName("Test loading configuration from environment variables")
    void testLoadFromEnvironmentVariables() {
        // This is going to be run from Maven Surefire, which allows setting environment variables in the pom.xml for testing purposes.
        assertDoesNotThrow(() -> {
            DrapiConfig config = DrapiConfig.builder()
                                            .applyEnvironmentVariables("DRAPI_")
                                            .build();
            assertNotNull(config.baseUrl(), "Base URL should be loaded from environment variables");
            assertNotNull(config.get("auth.token", String.class).orElse(null), "Token should be loaded from environment variables");
        }, "Should not throw exception when loading from environment variables");
    }

}
