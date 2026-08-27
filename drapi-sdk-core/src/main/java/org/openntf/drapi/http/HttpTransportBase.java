package org.openntf.drapi.http;

import java.util.Objects;
import java.util.concurrent.Executor;
import org.openntf.drapi.DrapiConfig;

public abstract class HttpTransportBase implements HttpTransport {

    private final DrapiConfig config;
    private final Executor executor;

    protected HttpTransportBase(DrapiConfig config, Executor executor) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
    }

    protected DrapiConfig config() {
        return config;
    }

    protected Executor executor() {
        return executor;
    }

}
