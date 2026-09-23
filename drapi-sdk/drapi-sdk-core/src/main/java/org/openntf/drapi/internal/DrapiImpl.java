package org.openntf.drapi.internal;

import java.util.Objects;
import org.openntf.drapi.Drapi;
import org.openntf.drapi.DrapiClient;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.auth.TokenSource;
import org.openntf.drapi.auth.SessionContext;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.internal.http.AuthenticatingHttpTransport;
import org.openntf.drapi.internal.log.Log;

public class DrapiImpl implements Drapi {

    private static final Log LOG = Log.getLogger(DrapiImpl.class);

    private final DrapiConfig config;
    private final HttpTransport transport;
    private final TokenSource tokenSource;

    public DrapiImpl(DrapiConfig config, HttpTransport httpTransport, TokenSource tokenSource) {
        this.config = Objects.requireNonNull(config, "Config must not be null");
        this.tokenSource = Objects.requireNonNull(tokenSource, "TokenSource must not be null");

        // transport will be the AuthenticatingHttpTransport that wraps the provided httpTransport and uses the tokenSource for authentication
        Objects.requireNonNull(httpTransport, "HttpTransport must not be null");
        this.transport = new AuthenticatingHttpTransport(httpTransport, tokenSource);
    }

    @Override
    public DrapiConfig config() {
        return config;
    }

    @Override
    public HttpTransport transport() {
        return transport;
    }

    @Override
    public DrapiClient forSession(SessionContext sessionContext) {
        return new DrapiClientImpl(this, sessionContext);
    }

    @Override
    public void logout(SessionContext sessionContext) {
        tokenSource.logout(sessionContext);
    }

    @Override
    public void shutdown() {
        // TODO implement resource cleanup if necessary (e.g., closing HTTP connections, releasing resources)
        try {
            // The real transport is wrapped in AuthenticatingHttpTransport. It will delegate the stop call to the underlying transport.
            transport().stop();
        } catch (Exception e) {
            // Handle exception during resource cleanup
            LOG.error("Failed to stop HttpTransport", e);
        }
    }
}
