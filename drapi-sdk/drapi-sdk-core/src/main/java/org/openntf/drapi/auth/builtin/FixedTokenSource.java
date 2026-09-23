package org.openntf.drapi.auth.builtin;

import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.auth.SessionContext;
import org.openntf.drapi.auth.Token;
import org.openntf.drapi.auth.TokenSourceBase;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.util.ConfigKey;
import org.openntf.drapi.util.TypeUtils;

public final class FixedTokenSource extends TokenSourceBase {

    private static final ConfigKey<String> AUTH_TOKEN_KEY = ConfigKey.of("auth.token", String.class);

    private final Token token;

    public FixedTokenSource(DrapiConfig config, HttpTransport transport) {
        super(config, transport);

        String tokenValue = config.get(AUTH_TOKEN_KEY).orElse(null);
        if (TypeUtils.isBlank(tokenValue)) {
            throw new IllegalArgumentException("Auth token must be provided in the configuration for FixedTokenSource.");
        }
        this.token = new Token(tokenValue, "fixed-token");
    }

    @Override
    public Token acquire(SessionContext sessionContext) {
        return token;
    }

    @Override
    public void tokenRejected(SessionContext sessionContext, Token token) {
        // No action needed for fixed token
    }

    @Override
    public void logout(SessionContext sessionContext) {
        // No action needed for fixed token
    }

    @Override
    public boolean supportsRefresh() {
        return false;
    }
}
