package org.openntf.drapi.http;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.openntf.drapi.DrapiConfig;

public abstract class HttpTransportBase implements HttpTransport {

    private final DrapiConfig config;
    private final Executor executor;

    protected HttpTransportBase(DrapiConfig config, Executor executor) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.executor = executor;
    }

    protected DrapiConfig config() {
        return config;
    }

    protected Optional<Executor> executor() {
        return Optional.ofNullable(executor);
    }

}
