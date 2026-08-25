package org.openntf.drapi.http;

import java.util.concurrent.Executor;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.internal.http.JdkHttpTransport;
import org.openntf.drapi.internal.http.JdkHttpTransport.JdkHttpTransportProvider;
import org.openntf.drapi.util.ServiceRegistry;

public interface HttpTransport {

    /**
     * Checks if there is an SPI-based implementation of HttpTransport available, and returns it if found. Otherwise, it returns the
     * default implementation (JdkHttpTransport).
     *
     * @return the default HttpTransport implementation
     */
    static HttpTransport defaultTransport(DrapiConfig config, Executor executor) {
        var provider = ServiceRegistry.findServiceOrDefault(HttpTransportProvider.class, JdkHttpTransportProvider::new);

        return provider.create(config, executor);
    }

}
