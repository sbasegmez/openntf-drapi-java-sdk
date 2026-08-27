package org.openntf.drapi.internal.http.jdk;

import java.util.concurrent.Executor;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.http.HttpTransportBase;
import org.openntf.drapi.http.HttpTransportProvider;

public class JdkHttpTransport extends HttpTransportBase {

    // TBD: Implement the actual HTTP transport logic using JDK's HttpClient or other relevant classes.

    JdkHttpTransport(DrapiConfig config, Executor executor) {
        super(config, executor);
    }

}
