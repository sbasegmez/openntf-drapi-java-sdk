package org.openntf.drapi.internal;

import java.util.ArrayList;
import java.util.List;
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
        this.baseUrl = TypeUtils.requireNonBlank(builder.baseUrl, "Base URL must not be blank");
        this.authScope = builder.authScope;
        this.authType = resolveAndValidateAuthType(builder);
        this.username = builder.username;
        this.password = builder.password;
        this.token = builder.token;
        this.appId = builder.appId;
        this.appSecret = builder.appSecret;

        this.userAgent = TypeUtils.defaultIfBlank(builder.userAgent, DEFAULT_USER_AGENT + "/" + Version.get());
        this.connectTimeoutSecs = builder.connectTimeoutSecs == 0 ? DEFAULT_CONNECT_TIMEOUT_SECS : builder.connectTimeoutSecs;
        this.requestTimeoutSecs = builder.requestTimeoutSecs == 0 ? DEFAULT_REQUEST_TIMEOUT_SECS : builder.requestTimeoutSecs;
    }

    private static AuthType resolveAndValidateAuthType(DrapiConfigBuilder builder) {
        List<AuthType> detectedTypes = new ArrayList<>();

        if (TypeUtils.isAllNonEmpty(builder.username, builder.password)) {
            detectedTypes.add(AuthType.BASIC);
        }

        if (TypeUtils.isNotEmpty(builder.token)) {
            detectedTypes.add(AuthType.TOKEN);
        }

        if (TypeUtils.isAllNonEmpty(builder.appId, builder.appSecret)) {
            detectedTypes.add(AuthType.OAUTH);
        }

        if (detectedTypes.isEmpty()) {
            throw new IllegalArgumentException("No valid authentication method provided. Please provide either BASIC, TOKEN, or OAUTH credentials.");
        } else if (detectedTypes.size() == 1) {
            return detectedTypes.get(0);
        } else {
            throw new IllegalArgumentException("Multiple authentication methods provided. Please provide only one: BASIC, TOKEN, or OAUTH.");
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
