package org.openntf.drapi;

import java.net.URI;
import org.openntf.drapi.internal.DrapiConfigBuilder;

public interface DrapiConfig {

    URI baseUrl();
    String authScope();
    AuthType authType();

    // For BASIC auth
    String username();
    String password();

    // For TOKEN auth
    String token();

    // For OAUTH auth
    String appId();

    String appSecret();

    String userAgent();

    int connectTimeoutSecs();

    int requestTimeoutSecs();

    enum AuthType {
        BASIC,
        TOKEN,
        OAUTH
    }

    static DrapiConfigBuilder builder() {
        return new DrapiConfigBuilder();
    }

}
