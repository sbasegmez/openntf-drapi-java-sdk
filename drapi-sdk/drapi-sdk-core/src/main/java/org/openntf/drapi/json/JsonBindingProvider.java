package org.openntf.drapi.json;

/**
 * SDK interface for JSON binding provider implementations. Implementations of this interface are expected to provide methods for
 * creating instances of JsonBinding.
 * <p>
 * This interface serves as a contract for JSON binding provider implementations, allowing for flexibility and interchangeability of
 * different JSON libraries or frameworks within the SDK.
 */
public interface JsonBindingProvider {

    /**
     * Creates a new instance of a JsonBinding implementation.
     *
     * @return a new JsonBinding instance
     */
    JsonBinding create();

}
