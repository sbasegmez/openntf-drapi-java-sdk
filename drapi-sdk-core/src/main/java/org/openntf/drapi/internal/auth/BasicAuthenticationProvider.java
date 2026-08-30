package org.openntf.drapi.internal.auth;

import java.util.concurrent.CompletableFuture;
import org.openntf.drapi.DrapiConfig;

public final class BasicAuthenticationProvider extends AuthenticationProviderBase {

    public BasicAuthenticationProvider(DrapiConfig config) {
        super(config);
    }

    @Override
    public CompletableFuture<BearerToken> acquireToken(AuthenticationToolkit toolkit) {
        return null;
    }

    @Override
    public boolean supportsRefresh() {
        return true;
    }

}
