package org.openntf.drapi.internal.auth;

import java.util.Map;
import org.openntf.drapi.DrapiConfig;

public final class TokenAuthenticationProvider extends AuthenticationProviderBase {

    public TokenAuthenticationProvider(DrapiConfig config) {
        super(config);
    }

    @Override
    public BearerToken acquireToken(AuthenticationToolkit toolkit) {
        // Token authentication provider simply returns the token from the config, as it is assumed to be a fixed, externally-obtained token.
        return new BearerToken(config.token(), Map.of());
    }

    @Override
    public boolean supportsRefresh() {
        // Token authentication provider does not support refresh, as it is assumed to be a fixed, externally-obtained token.
        return false;
    }

}
