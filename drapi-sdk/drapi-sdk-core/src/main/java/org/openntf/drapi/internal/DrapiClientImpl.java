package org.openntf.drapi.internal;

import java.util.Objects;
import org.openntf.drapi.DrapiClient;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.DrapiContext;

public class DrapiClientImpl implements DrapiClient {

    private final DrapiContext context;

    DrapiClientImpl(DrapiConfig config, DrapiContext context) {
        this.context = Objects.requireNonNull(context, "Context must not be null");
    }

    DrapiContext context() {
        return context;
    }

    DrapiConfig config() {
        return context.config();
    }

}
