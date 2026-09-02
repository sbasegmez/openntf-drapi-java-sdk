package org.openntf.drapi.http;

import java.util.concurrent.Executor;
import org.openntf.drapi.DrapiConfig;

public interface HttpTransportProvider {

    HttpTransport create(DrapiConfig config, Executor executor);

}
