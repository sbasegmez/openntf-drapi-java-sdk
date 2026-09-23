package org.openntf.drapi.auth;

import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.http.HttpTransport;

public abstract non-sealed class TokenSourceBase implements TokenSource {

    protected final DrapiConfig config;
    protected final HttpTransport transport;

    protected TokenSourceBase(DrapiConfig config, HttpTransport transport) {
        this.config = config;
        this.transport = transport;
    }

    protected DrapiConfig config() {
        return this.config;
    }

    protected HttpTransport transport() {
        return this.transport;
    }

}
