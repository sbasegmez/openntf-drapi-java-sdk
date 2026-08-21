package org.openntf.drapi.json;

/**
 * SDK interface for JSON processor provider implementations. Implementations of this interface are expected to provide methods for
 * creating instances of JsonProcessor.
 * <p>
 * This interface serves as a contract for JSON processor provider implementations, allowing for flexibility and interchangeability of
 * different JSON libraries or frameworks within the SDK.
 */
public interface JsonProcessorProvider {

    /**
     * Creates a new instance of a JsonProcessor implementation.
     *
     * @return a new JsonProcessor instance
     */
    JsonProcessor create();

}
