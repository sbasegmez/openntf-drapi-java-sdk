package org.openntf.drapi.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.util.TypeUtils;

public class DrapiConfigBuilder {

    // Baseline
    URI baseUrl;
    String authScope;

    // BASIC auth
    String username;
    String password;

    // TOKEN auth
    String token;

    // OAUTH auth
    String appId;
    String appSecret;

    // Others
    String userAgent;
    int connectTimeoutSecs = 0;
    int requestTimeoutSecs = 0;

    public DrapiConfigBuilder baseUrl(String baseUrl) {
        this.baseUrl = URI.create(Objects.requireNonNull(baseUrl));
        return this;
    }

    public DrapiConfigBuilder baseUrl(URI baseUrl) {
        this.baseUrl = Objects.requireNonNull(baseUrl);
        return this;
    }

    public DrapiConfigBuilder authScope(String authScope) {
        this.authScope = authScope;
        return this;
    }

    public DrapiConfigBuilder basic(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    public DrapiConfigBuilder token(String token) {
        this.token = token;
        return this;
    }

    public DrapiConfigBuilder oauth(String appId, String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
        return this;
    }

    public DrapiConfigBuilder userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public DrapiConfigBuilder connectTimeout(int seconds) {
        this.connectTimeoutSecs = seconds;
        return this;
    }

    public DrapiConfigBuilder connectTimeout(Duration duration) {
        return connectTimeout((int) duration.getSeconds());
    }

    public DrapiConfigBuilder requestTimeout(int seconds) {
        this.requestTimeoutSecs = seconds;
        return this;
    }

    public DrapiConfigBuilder requestTimeout(Duration duration) {
        return requestTimeout((int) duration.getSeconds());
    }

    public DrapiConfigBuilder applyEnvironmentVariables(String prefix) {
        return applyMap(System.getenv(), prefix);
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

    public DrapiConfigBuilder applyFile(File file) {
        try (var inputStream = new FileInputStream(file)) {
            return applyResourceFile(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from file: " + file.getAbsolutePath(), e);
        }
    }

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
        applyMap(properties, "");

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
     * @param map    Map of properties to apply to the builder.
     * @param prefix Prefix to filter relevant properties
     * @return The current DrapiConfigBuilder instance
     */
    DrapiConfigBuilder applyMap(Map<?, ?> map, String prefix) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString().toLowerCase(Locale.ENGLISH);
            if (null!=prefix && !TypeUtils.startsWithIgnoreCase(key, prefix)) {
                continue; // Skip keys that don't match the prefix
            }

            String strippedKey = TypeUtils.isEmpty(prefix) ? key : key.substring(prefix.length());
            String value = entry.getValue().toString();

            switch (strippedKey) {
                case "baseurl" -> this.baseUrl(value);
                case "authscope" -> this.authScope(value);
                case "username" -> this.username = value;
                case "password" -> this.password = value;
                case "token" -> this.token = value;
                case "appid" -> this.appId = value;
                case "appsecret" -> this.appSecret = value;
                case "useragent" -> this.userAgent(value);
                case "connecttimeoutsecs" -> {
                    if (TypeUtils.isNumeric(value)) {
                        this.connectTimeout(Integer.parseInt(value));
                    }
                }
                case "requesttimeoutsecs" -> {
                    if (TypeUtils.isNumeric(value)) {
                        this.requestTimeout(Integer.parseInt(value));
                    }
                }
                default -> {  /* Ignore unknown properties or log a warning if needed */ }
            }
        }
        return this;
    }

}

