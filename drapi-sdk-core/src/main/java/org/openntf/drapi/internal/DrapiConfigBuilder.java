package org.openntf.drapi.internal;

import java.time.Duration;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.DrapiConfig.AuthType;

public class DrapiConfigBuilder {

    // Baseline
    String baseUrl;
    String authScope;
    AuthType authType;

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
        this.baseUrl = baseUrl;
        return this;
    }

    public DrapiConfigBuilder authScope(String authScope) {
        this.authScope = authScope;
        return this;
    }

    public DrapiConfigBuilder basic(String username, String password) {
        this.authType = AuthType.BASIC;
        this.username = username;
        this.password = password;
        return this;
    }

    public DrapiConfigBuilder token(String token) {
        this.authType = AuthType.TOKEN;
        this.token = token;
        return this;
    }

    public DrapiConfigBuilder oauth(String appId, String appSecret) {
        this.authType = AuthType.OAUTH;
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

    public DrapiConfig build() {
        return new DrapiConfigImpl(this);
    }
}
