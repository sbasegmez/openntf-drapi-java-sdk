package org.openntf.drapi.http;

import java.util.Objects;
import java.util.concurrent.Executor;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.auth.TokenSource;

/**
 * Represent context information for the transport layer, such as configuration settings, authentication details, and other relevant
 * data needed for making HTTP requests.
 */
public record TransportContext(
    DrapiConfig config,
    TokenSource tokenSource,
    Executor executor
) {

    public TransportContext {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(tokenSource, "tokenSource must not be null");
    }
}
