package org.openntf.json.jakarta;

import org.openntf.drapi.json.JsonProcessor;
import org.openntf.drapi.json.JsonProcessorProvider;

public class JakartaJsonProcessorProvider implements JsonProcessorProvider {

    /**
     * Creates a new instance of a JsonProcessor implementation.
     *
     * @return a new JsonProcessor instance
     */
    @Override
    public JsonProcessor create() {
        return new JakartaJsonProcessor();
    }
}
