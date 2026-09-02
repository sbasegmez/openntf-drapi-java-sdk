package org.openntf.drapi.sample;

import org.openntf.drapi.DrapiClient;
import org.openntf.drapi.DrapiConfig;

public class StandaloneExample {

    public static void main(String[] args) {
        DrapiConfig config = DrapiConfig.builder()
                                        .applyResourceFile("config/drapi-sdk-sample.properties")
                                        .build();

        DrapiClient client = DrapiClient.builder(config)
                                        .build();


    }


}
