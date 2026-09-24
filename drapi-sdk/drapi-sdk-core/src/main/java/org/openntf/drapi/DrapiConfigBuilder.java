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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;
import org.openntf.drapi.internal.DrapiConfigImpl;
import org.openntf.drapi.internal.log.Log;
import org.openntf.drapi.util.ConfigKey;
import org.openntf.drapi.util.TypeUtils;

public class DrapiConfigBuilder {

    private static final Log LOG = Log.getLogger(DrapiConfigBuilder.class);

    // Required parameters
    private URI baseUrl;

    // Optional authentication parameters
    private String userAgent;
    private int connectTimeoutSecs = 0;
    private int requestTimeoutSecs = 0;

    // Extra optional parameters for extensibility
    private final Map<String, Object> extraParams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Set the base URL for the API. This is a required parameter and must be set before building the DrapiConfig.
     *
     * @param baseUrl The base URL for the API as a String
     * @return The current DrapiConfigBuilder instance
     */
    public DrapiConfigBuilder baseUrl(String baseUrl) {
        this.baseUrl = URI.create(Objects.requireNonNull(baseUrl, "baseUrl cannot be null"));
        return this;
    }

    /**
     * Set the base URL for the API. This is a required parameter and must be set before building the DrapiConfig.
     *
     * @param baseUrl The base URL for the API as a URI
     * @return The current DrapiConfigBuilder instance
     */
    public DrapiConfigBuilder baseUrl(URI baseUrl) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl cannot be null");
        return this;
    }

    /**
     * Set the User-Agent header for the API requests. This is an optional parameter.
     *
     * @param userAgent The User-Agent string to be used in API requests
     * @return The current DrapiConfigBuilder instance
     */
    public DrapiConfigBuilder userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Set the connection timeout for the API requests. This is an optional parameter.
     *
     * @param seconds The connection timeout in seconds
     * @return The current DrapiConfigBuilder instance
     */
    public DrapiConfigBuilder connectTimeout(int seconds) {
        if(seconds <= 0) {
            throw new IllegalArgumentException("Connect timeout cannot be negative or zero");
        }

        this.connectTimeoutSecs = seconds;
        return this;
    }

    /**
     * Set the connection timeout for the API requests. This is an optional parameter.
     *
     * @param duration The connection timeout as a Duration
     * @return The current DrapiConfigBuilder instance
     */
    public DrapiConfigBuilder connectTimeout(Duration duration) {
        return connectTimeout((int) duration.getSeconds());
    }

    /**
     * Set the request timeout for the API requests. This is an optional parameter.
     *
     * @param seconds The request timeout in seconds
     * @return The current DrapiConfigBuilder instance
     */
    public DrapiConfigBuilder requestTimeout(int seconds) {
        if(seconds <= 0) {
            throw new IllegalArgumentException("Request timeout cannot be negative or zero");
        }

        this.requestTimeoutSecs = seconds;
        return this;
    }

    /**
     * Set the request timeout for the API requests. This is an optional parameter.
     *
     * @param duration The request timeout as a Duration
     * @return The current DrapiConfigBuilder instance
     */
    public DrapiConfigBuilder requestTimeout(Duration duration) {
        return requestTimeout((int) duration.getSeconds());
    }

    /**
     * Add an extra parameter to the API requests. This is an optional parameter.
     * <p>
     * Extra parameters can be used to pass additional information to the extensions of this SDK. The keys are case-insensitive and will
     * be stored in a TreeMap with case-insensitive ordering.
     *
     * @param key   The key of the extra parameter
     * @param value The value of the extra parameter
     * @return The current DrapiConfigBuilder instance
     */
    public DrapiConfigBuilder addExtraParam(String key, Object value) {
        extraParams.put(key, value);
        return this;
    }

    /**
     * Add multiple extra parameters to the API requests. This is an optional parameter.
     * <p>
     * Extra parameters can be used to pass additional information to the extensions of this SDK. The keys are case-insensitive and will
     * be stored in a TreeMap with case-insensitive ordering.
     *
     * @param params A map of extra parameters to add
     * @return The current DrapiConfigBuilder instance
     */
    public DrapiConfigBuilder addExtraParams(Map<String, Object> params) {
        extraParams.putAll(params);
        return this;
    }

    /**
     * Add an extra parameter to the API requests using a ConfigKey. This is an optional parameter.
     * <p>
     * Extra parameters can be used to pass additional information to the extensions of this SDK. The keys are case-insensitive and will
     * be stored in a TreeMap with case-insensitive ordering.
     *
     * @param key   The ConfigKey of the extra parameter
     * @param value The value of the extra parameter
     * @param <T>   The type of the value associated with the ConfigKey
     * @return The current DrapiConfigBuilder instance
     */
    public <T> DrapiConfigBuilder addExtraParam(ConfigKey<T> key, T value) {
        extraParams.put(key.key(), value);
        return this;
    }

    /**
     * Apply environment variables to the builder's fields, filtering by a specified prefix. This method will read the system's
     * environment variables and apply those that start with the given prefix to the builder's fields.
     * <p>
     * After removing prefix, all underscores in the environment variable names will be replaced with dots, and the names will be
     * converted to lowercase.
     * <p>
     * So, if the prefix is "DRAPI_", an environment variable named "DRAPI_AUTH_USERNAME" will be transformed to "auth.username".
     *
     * @param prefix The prefix to filter environment variables by
     * @return The current DrapiConfigBuilder instance
     */
    public DrapiConfigBuilder applyEnvironmentVariables(String prefix) {
        return doApplyEnvironmentVariables(System.getenv(), prefix);
    }

    // Testable version of applyEnvironmentVariables, which allows passing a custom environment variable map for testing purposes.
    protected DrapiConfigBuilder doApplyEnvironmentVariables(Map<String, String> envVars, String prefix) {
        TypeUtils.requireNonEmpty(prefix, "Prefix cannot be null or empty");

        Map<String, String> filteredEnvVars = new HashMap<>();

        envVars.entrySet()
               .stream()
               .filter(entry -> TypeUtils.startsWithIgnoreCase(entry.getKey(), prefix))
               .map(entry -> Map.entry(entry.getKey()
                                            .toLowerCase(Locale.ENGLISH)
                                            .replace(prefix.toLowerCase(Locale.ENGLISH), "")
                                            .replace("_", "."), entry.getValue()))
               .forEach(entry -> filteredEnvVars.put(entry.getKey(), entry.getValue()));

        return applyMap(filteredEnvVars);
    }

    /**
     * Apply properties from a resource file located in the classpath to the builder's fields. This method will load the properties from
     * the specified resource file and apply them to the builder
     * <p>
     * TODO Test this method from Eclipse/OSGi environment and from a JAR file to ensure it works in both scenarios.
     *
     * @param relativeFilePath The relative path to the resource file in the classpath (e.g., "config/drapi.properties")
     * @return The current DrapiConfigBuilder instance
     */
    public DrapiConfigBuilder applyResourceFile(String relativeFilePath) {
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(relativeFilePath)) {
            if (inputStream != null) {
                return applyResourceFile(inputStream);
            } else {
                throw new IllegalArgumentException("Resource not found: " + relativeFilePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from resource: " + relativeFilePath, e);
        }
    }

    /**
     * Apply properties from a file located in the filesystem to the builder's fields. This method will load the properties from the
     * specified file and apply them to the builder.
     *
     * @param file The file containing properties to apply to the builder
     * @return The current DrapiConfigBuilder instance
     */
    public DrapiConfigBuilder applyFile(File file) {
        try (var inputStream = new FileInputStream(file)) {
            return applyResourceFile(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Builds and returns a DrapiConfig instance based on the current state of the builder. This method will validate that all required
     * parameters have been set and will throw an exception if any required parameter is missing.
     *
     * @return A DrapiConfig instance with the configured parameters
     */
    public DrapiConfig build() {
        return new DrapiConfigImpl(this);
    }

    /**
     * Apply properties from an InputStream to the builder's fields.
     *
     * @param inputStream InputStream containing properties to apply to the builder. Stream will not be closed by this method.
     * @return The current DrapiConfigBuilder instance
     * @throws IOException if an I/O error occurs when reading from the InputStream
     */
    private DrapiConfigBuilder applyResourceFile(InputStream inputStream) throws IOException {
        Properties properties = new Properties();
        properties.load(inputStream);
        applyMap(properties);

        return this;
    }

    /**
     * Apply properties from a Map to the builder's fields, using a specified prefix to filter relevant properties. This is supposed to
     * be used internally and for testing purposes, hence package-private access. It will apply properties either from a Properties
     * object generated via a properties file or environment variables.
     * <p>
     * Map generics are not well-defined in Properties interface. So we use Map<?, ?> to accommodate both Properties and environment
     * variable maps.
     *
     * @param map Map of properties to apply to the builder.
     * @return The current DrapiConfigBuilder instance
     */
    DrapiConfigBuilder applyMap(Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString().toLowerCase(Locale.ENGLISH);
            String value = entry.getValue().toString();

            switch (key) {
                case "baseurl" -> this.baseUrl(value);
                case "useragent" -> this.userAgent(value);
                case "connecttimeoutsecs" -> {
                    if (TypeUtils.isNumeric(value)) {
                        this.connectTimeout(Integer.parseInt(value));
                    } else {
                        LOG.warn("Invalid connect timeout value: {}", value);
                    }
                }
                case "requesttimeoutsecs" -> {
                    if (TypeUtils.isNumeric(value)) {
                        this.requestTimeout(Integer.parseInt(value));
                    } else {
                        LOG.warn("Invalid request timeout value: {}", value);
                    }

                }
                // For any other keys, we store them in the extraParams map
                default -> this.addExtraParam(key, value);
            }
        }
        return this;
    }

    /**
     * Get the base URL for the API. This is a required parameter and must be set before building the DrapiConfig.
     *
     * @return The base URL for the API as a URI
     */
    public URI baseUrl() {
        return baseUrl;
    }

    /**
     * Get the User-Agent string for the API requests.
     *
     * @return The User-Agent string
     */
    public String userAgent() {
        return userAgent;
    }

    /**
     * Get the connection timeout in seconds for the API requests.
     *
     * @return The connection timeout in seconds
     */
    public int connectTimeoutSecs() {
        return connectTimeoutSecs;
    }

    /**
     * Get the request timeout in seconds for the API requests.
     *
     * @return The request timeout in seconds
     */
    public int requestTimeoutSecs() {
        return requestTimeoutSecs;
    }

    /**
     * Get the extra parameters for the API requests.
     *
     * @return A map of extra parameters
     */
    public Map<String, Object> extraParams() {
        TreeMap<String, Object> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(extraParams);
        return map;
    }
}

