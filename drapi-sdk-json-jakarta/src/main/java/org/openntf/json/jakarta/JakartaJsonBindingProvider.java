package org.openntf.json.jakarta;

import org.openntf.drapi.json.JsonBinding;
import org.openntf.drapi.json.JsonBindingProvider;

public class JakartaJsonBindingProvider implements JsonBindingProvider {

    /**
     * Creates a new instance of a JsonBinding implementation.
     *
     * @return a new JsonBinding instance
     */
    @Override
    public JsonBinding create() {
        return new JakartaJsonBinding();
    }
}
