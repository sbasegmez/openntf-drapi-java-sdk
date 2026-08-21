package org.openntf.drapi.json;

import org.openntf.drapi.util.ServiceRegistry;

/**
 * SDK interface for JSON processor implementations. Implementations of this interface are expected to provide methods for serializing
 * and deserializing JSON data, as well as any other necessary functionality related to JSON processing.
 * <p>
 * This interface serves as a contract for JSON processor implementations, allowing for flexibility and interchangeability of different
 * JSON libraries or frameworks within the SDK.
 * <p>
 * Use JsonProcessorProvider to obtain an instance of a JsonProcessor implementation.
 */
public interface JsonProcessor {



    static JsonProcessor get() {
        return ServiceRegistry.findService(JsonProcessorProvider.class)
                              .create();
    }

}
