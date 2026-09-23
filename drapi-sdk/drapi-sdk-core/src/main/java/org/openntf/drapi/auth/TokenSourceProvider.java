package org.openntf.drapi.auth;

import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.http.HttpTransport;

public interface TokenSourceProvider {

    /**
     * Creates a TokenSource instance based on the provided DrapiConfig and HttpTransport.
     * <p>
     * Implementations of any TokenSource should provide a concrete implementation of this method to create and return a
     * TokenSource instance. So we can guarantee the TokenSource is created with the correct configuration and transport.
     *
     * @param config    DrapiConfig containing configuration settings for the TokenSource
     * @param transport HttpTransport used for making network requests
     * @return a TokenSource instance
     */
    TokenSource create(DrapiConfig config, HttpTransport transport);

}
