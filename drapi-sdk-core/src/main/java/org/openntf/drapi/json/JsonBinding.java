package org.openntf.drapi.json;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.openntf.drapi.util.ServiceRegistry;

/**
 * SDK interface for JSON implementations. Implementations of this interface are expected to provide methods for serializing and
 * deserializing JSON data, as well as any other necessary functionality related to JSON processing.
 * <p>
 * This interface serves as a contract for JSON implementations, allowing for flexibility and interchangeability of different JSON
 * libraries or frameworks within the SDK.
 * <p>
 * Use JsonBindingProvider to obtain an instance of a JsonBinding implementation.
 */
public interface JsonBinding {

    String name();

    Map<String, Object> fromJson(InputStream jsonStream);
    <T> T fromJson(InputStream jsonStream, Class<T> valueType);

    void toJson(Object objectValue, OutputStream outputStream);

    static JsonBinding create() {
        return ServiceRegistry.findService(JsonBindingProvider.class)
                              .create();
    }

    default Map<String, Object> fromJson(String jsonString) {
        Objects.requireNonNull(jsonString, "JSON string cannot be null");
        return fromJson(
            new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8))
        );
    }

    default <T> T fromJson(String jsonString, Class<T> valueType) {
        Objects.requireNonNull(jsonString, "JSON string cannot be null");
        Objects.requireNonNull(valueType, "Value type cannot be null");
        return fromJson(
            new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)),
            valueType
        );
    }

    default String toJson(Object objectValue) {
        Objects.requireNonNull(objectValue, "Object value cannot be null");
        var outputStream = new java.io.ByteArrayOutputStream();
        toJson(objectValue, outputStream);
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}
