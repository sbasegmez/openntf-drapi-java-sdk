package org.openntf.drapi.internal;

import java.util.Objects;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.util.TypeUtils;

public class DrapiConfigImpl implements DrapiConfig {

    public static final String DEFAULT_USER_AGENT = "OPENNTF-DRAPI-SDK-JAVA";
    public static final int DEFAULT_CONNECT_TIMEOUT_SECS = 5;
    public static final int DEFAULT_REQUEST_TIMEOUT_SECS = 15;

    // Baseline
    private final String baseUrl;
    private final String authScope;
    private final AuthType authType;

    // BASIC auth
    private final String username;
    private final String password;

    // TOKEN auth
    private final String token;

    // OAUTH auth
    private final String appId;
    private final String appSecret;

    // Others

    // Version tag will be appended in the constructor
    private final String userAgent;
    private final int connectTimeoutSecs;
    private final int requestTimeoutSecs;

    DrapiConfigImpl(DrapiConfigBuilder builder) {
        validateConfig(builder);

        this.baseUrl = builder.baseUrl;
        this.authScope = builder.authScope;
        this.authType = builder.authType;
        this.username = builder.username;
        this.password = builder.password;
        this.token = builder.token;
        this.appId = builder.appId;
        this.appSecret = builder.appSecret;

        this.userAgent = TypeUtils.defaultIfBlank(builder.userAgent, DEFAULT_USER_AGENT) + "/" + Version.get();
        this.connectTimeoutSecs = builder.connectTimeoutSecs == 0 ? DEFAULT_CONNECT_TIMEOUT_SECS : builder.connectTimeoutSecs;
        this.requestTimeoutSecs = builder.requestTimeoutSecs == 0 ? DEFAULT_REQUEST_TIMEOUT_SECS : builder.requestTimeoutSecs;
    }

    private void validateConfig(DrapiConfigBuilder builder) {
        TypeUtils.requireNonBlank(builder.baseUrl, "Base URL must not be blank");

        switch(Objects.requireNonNull(builder.authType, "Auth type must not be null")) {
            case BASIC:
                TypeUtils.requireNonBlank(builder.username, "Username must not be blank for BASIC auth");
                TypeUtils.requireNonBlank(builder.password, "Password must not be blank for BASIC auth");
                break;
            case TOKEN:
                TypeUtils.requireNonBlank(builder.token, "Token must not be blank for TOKEN auth");
                break;
            case OAUTH:
                TypeUtils.requireNonBlank(builder.appId, "App ID must not be blank for OAUTH auth");
                TypeUtils.requireNonBlank(builder.appSecret, "App Secret must not be blank for OAUTH auth");
                break;
            default:
                // Dead code, but just in case
                throw new IllegalArgumentException("Unsupported auth type: " + builder.authType);
        }
    }

    @Override
    public String baseUrl() {
        return baseUrl;
    }

    @Override
    public AuthType authType() {
        return authType;
    }

    @Override
    public String authScope() {
        return authScope;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public String token() {
        return token;
    }

    @Override
    public String appId() {
        return appId;
    }

    @Override
    public String appSecret() {
        return appSecret;
    }

    @Override
    public String userAgent() {
        return userAgent;
    }

    @Override
    public int connectTimeoutSecs() {
        return connectTimeoutSecs;
    }

    @Override
    public int requestTimeoutSecs() {
        return requestTimeoutSecs;
    }
}
