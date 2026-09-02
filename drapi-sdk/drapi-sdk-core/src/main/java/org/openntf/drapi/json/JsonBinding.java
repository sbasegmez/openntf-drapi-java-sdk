package org.openntf.drapi.json;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
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

    default Map<String, Object> fromJson(String jsonString) {
        return fromJson(
            new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8))
        );
    }

    default <T> T fromJson(String jsonString, Class<T> valueType) {
        return fromJson(
            new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)),
            valueType
        );
    }

    default String toJson(Object objectValue) {
        var outputStream = new java.io.ByteArrayOutputStream();
        toJson(objectValue, outputStream);
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    static JsonBinding get() {
        return JsonBindingHolder.getInstance();
    }

    final class JsonBindingHolder {
        // Lazy-loaded singleton instance of JsonBinding
        private static volatile JsonBinding defaultInstance;

        // This can be overridden by a test
        private static volatile JsonBinding overriddenInstance;

        static JsonBinding getInstance() {
            if (overriddenInstance != null) {
                return overriddenInstance;
            }
            if (defaultInstance == null) {
                synchronized (JsonBindingHolder.class) {
                    if (defaultInstance == null) {
                        defaultInstance = ServiceRegistry.findService(JsonBindingProvider.class).create();
                    }
                }
            }
            return defaultInstance;
        }

        // package-private method to allow tests to override the JsonBinding instance
        static void override(JsonBinding jsonBinding) {
            overriddenInstance = jsonBinding;
        }

        // package-private method to reset the overridden instance, allowing tests to clean up after themselves
        static void reset() {
            overriddenInstance = null;
        }

        private JsonBindingHolder() {}
    }

}
