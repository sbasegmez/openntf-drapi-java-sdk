package org.openntf.drapi;

import org.openntf.drapi.internal.DrapiClientBuilder;

public interface DrapiClient {


    static DrapiClientBuilder builder(DrapiConfig config) {
        return new DrapiClientBuilder(config);
    }

}
