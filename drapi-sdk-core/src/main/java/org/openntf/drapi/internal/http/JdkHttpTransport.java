package org.openntf.drapi.internal.http;

import java.util.concurrent.Executor;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.http.HttpTransportProvider;

public class JdkHttpTransport implements HttpTransport {

    private final DrapiConfig config;
    private final Executor executor;

    // TBD: Implement the actual HTTP transport logic using JDK's HttpClient or other relevant classes.

    public JdkHttpTransport(DrapiConfig config, Executor executor) {
        this.config = config;
        this.executor = executor;
    }

    public static class JdkHttpTransportProvider implements HttpTransportProvider {

        @Override
        public HttpTransport create(DrapiConfig config, Executor executor) {
            return new JdkHttpTransport(config, executor);
        }
    }

}
