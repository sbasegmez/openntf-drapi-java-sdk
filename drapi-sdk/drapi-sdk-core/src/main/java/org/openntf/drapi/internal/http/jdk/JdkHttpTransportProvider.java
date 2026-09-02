package org.openntf.drapi.internal.http.jdk;

import java.util.concurrent.Executor;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.http.HttpTransportProvider;

public class JdkHttpTransportProvider implements HttpTransportProvider {

    @Override
    public HttpTransport create(DrapiConfig config, Executor executor) {
        return new JdkHttpTransport(config, executor);
    }
}
