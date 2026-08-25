package org.openntf.drapi.internal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.DrapiConfig.AuthType;

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

    @Test
    @DisplayName("Test loading configuration from properties file")
    void testLoadFromPropertiesFile() {
        DrapiConfig config = DrapiConfig.builder()
                                        .applyResourceFile("config/example.properties")
                                        .build();

        assertEquals("https://api.example.com", config.baseUrl(), "Base URL should match the properties file");
        assertEquals("$DATA", config.authScope(), "Auth scope should match the properties file");
        assertEquals(DrapiConfig.AuthType.BASIC, config.authType(), "Auth type should match the properties file");
        assertEquals("your_username", config.username(), "Username should match the properties file");
        assertEquals("your_password", config.password(), "Password should match the properties file");
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
                                        ), null)
                                        .build();

        assertEquals("https://api.example.com", config.baseUrl(), "Base URL should match the map");
        assertEquals(DrapiConfig.AuthType.BASIC, config.authType(), "Auth type should match the map");
        assertEquals("your_username", config.username(), "Username should match the map");
        assertEquals("your_password", config.password(), "Password should match the map");
        assertTrue(config.userAgent()
                         .startsWith(DrapiConfigImpl.DEFAULT_USER_AGENT), "User agent should fallback to default due to blank value");
        assertEquals(DrapiConfigImpl.DEFAULT_CONNECT_TIMEOUT_SECS, config.connectTimeoutSecs(), "Connect timeout should fallback to default due to invalid value");
        assertEquals(DrapiConfigImpl.DEFAULT_REQUEST_TIMEOUT_SECS, config.requestTimeoutSecs(), "Request timeout should fallback to default due to invalid value");
    }

    @Test
    @DisplayName("Test loading configuration from map with Oauth parameters")
    void testLoadFromMapWithOAuth() {
        var config = DrapiConfig.builder()
                                .applyMap(Map.of(
                                    "DRAPI_BASEURL", "https://api.example.com",
                                    "DRAPI_AUTHSCOPE", "$DATA",
                                    "DRAPI_APPID", "your_ap_id",
                                    "DRAPI_APPSECRET", "your_secret",
                                    "DRAPI_USERAGENT", "MyApp"
                                ), "DRAPI_")
                                .build();

        assertEquals(AuthType.OAUTH, config.authType(), "Auth type should be OAUTH");
        assertEquals("https://api.example.com", config.baseUrl(), "Base URL should match the map");
        assertEquals("$DATA", config.authScope(), "Auth scope should match the map");
        assertEquals("your_ap_id", config.appId(), "App ID should match the map");
        assertEquals("your_secret", config.appSecret(), "App Secret should match the map");
        assertEquals("MyApp", config.userAgent(), "User agent should match the map");
    }

    @Test
    @DisplayName("Test loading configuration from map with multiple auth types")
    void testLoadFromMapWithMultipleAuth() {
        assertThrows(IllegalArgumentException.class,
                     () -> DrapiConfig.builder()
                                      .applyMap(Map.of(
                                          "DRAPI_BASEURL", "https://api.example.com",
                                          "DRAPI_USERNAME", "your_username",
                                          "DRAPI_PASSWORD", "your_password",
                                          "DRAPI_TOKEN", "your_token" // Both BASIC and TOKEN auth provided
                                      ), "DRAPI_")
                                      .build(), "Should throw exception due to multiple auth types");
    }

    @EnabledIfEnvironmentVariable(named = "DRAPI_BASEURL", matches = ".*")
    @EnabledIfEnvironmentVariable(named = "DRAPI_TOKEN", matches = ".*")
    @Test
    @DisplayName("Test loading configuration from environment variables")
    void testLoadFromEnvironmentVariables() {
        // This is going to be run from Maven Surefire, which allows setting environment variables in the pom.xml for testing purposes.
        assertDoesNotThrow(() -> {
            DrapiConfig config = DrapiConfig.builder()
                                            .applyEnvironmentVariables("DRAPI_")
                                            .build();
            assertNotNull(config.baseUrl(), "Base URL should be loaded from environment variables");
            assertNotNull(config.authType(), "Auth type should be determined from environment variables");
        }, "Should not throw exception when loading from environment variables");
    }

}
