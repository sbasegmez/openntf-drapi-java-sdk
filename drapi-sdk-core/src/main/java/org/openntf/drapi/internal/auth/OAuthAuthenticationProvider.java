package org.openntf.drapi.internal.auth;

import java.util.concurrent.CompletableFuture;
import org.openntf.drapi.DrapiConfig;

public final class OAuthAuthenticationProvider extends AuthenticationProviderBase {

    public OAuthAuthenticationProvider(DrapiConfig config) {
        super(config);
        throw new UnsupportedOperationException("OAuth authentication is not yet implemented.");
    }

    @Override
    public CompletableFuture<BearerToken> acquireToken(AuthenticationToolkit toolkit) {
        return null;
    }

    @Override
    public boolean supportsRefresh() {
        return false;
    }
}
