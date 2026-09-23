package org.openntf.drapi.auth.builtin;

import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.auth.TokenSource;
import org.openntf.drapi.auth.TokenSourceProvider;
import org.openntf.drapi.http.HttpTransport;

public class PasswordTokenSourceProvider implements TokenSourceProvider {

    @Override
    public TokenSource create(DrapiConfig config, HttpTransport transport) {
        return new PasswordTokenSource(config, transport);
    }

}
