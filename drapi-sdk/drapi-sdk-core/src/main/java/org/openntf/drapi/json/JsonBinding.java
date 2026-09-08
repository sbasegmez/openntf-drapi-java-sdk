/*
 * Copyright (c) 2026 Serdar Basegmez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    /**
     * Returns the name of the JSON implementation. This name can be used to identify the specific JSON library or framework being
     * used.
     *
     * @return the name of the JSON implementation
     */
    String name();

    /**
     * Deserializes JSON data from the provided InputStream into a Map<String, Object>. The resulting map represents the JSON structure,
     * where keys are JSON field names and values are the corresponding values.
     *
     * @param jsonStream the InputStream containing the JSON data
     * @return a Map representing the deserialized JSON structure
     */
    Map<String, Object> fromJson(InputStream jsonStream);

    /**
     * Deserializes JSON data from the provided InputStream into an instance of the specified valueType. The resulting object is
     * populated with the data from the JSON structure.
     * <p>
     * This is useful to serialise simple Json objects into a Java object of a specific type, allowing for type-safe access to the data.
     * Since we cannot support library-specific annotations in this project, more advanced serialisation/deserialisation features (like
     * custom field names, ignoring fields, etc.) are not supported. For more advanced use cases, consider using the underlying JSON
     * library directly.
     *
     * @param jsonStream the InputStream containing the JSON data
     * @param valueType  the class of the type to deserialize into
     * @param <T>        the type of the resulting object
     * @return an instance of the specified type populated with the JSON data
     */
    <T> T fromJson(InputStream jsonStream, Class<T> valueType);

    /**
     * Serializes the provided objectValue into JSON format and writes it to the provided OutputStream. The objectValue can be any Java
     * object that can be represented in JSON format.
     *
     * @param objectValue  the object to be serialized into JSON
     * @param outputStream the OutputStream to write the JSON data to
     */
    void toJson(Object objectValue, OutputStream outputStream);

    /**
     * Deserializes JSON data from the provided jsonString into a Map<String, Object>. The resulting map represents the JSON structure,
     * where keys are JSON field names and values are the corresponding values.
     *
     * @param jsonString the JSON string to be deserialized
     * @return a Map representing the deserialized JSON structure
     */
    default Map<String, Object> fromJson(String jsonString) {
        return fromJson(
            new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8))
        );
    }

    /**
     * Deserializes JSON data from the provided jsonString into an instance of the specified valueType. The resulting object is
     * populated with the data from the JSON structure.
     * <p>
     * This is useful to serialise simple Json objects into a Java object of a specific type, allowing for type-safe access to the data.
     * Since we cannot support library-specific annotations in this project, more advanced serialisation/deserialisation features (like
     * custom field names, ignoring fields, etc.) are not supported. For more advanced use cases, consider using the underlying JSON
     * library directly.
     *
     * @param jsonString the JSON string to be deserialized
     * @param valueType  the class of the type to deserialize into
     * @param <T>        the type of the resulting object
     * @return an instance of the specified type populated with the JSON data
     */
    default <T> T fromJson(String jsonString, Class<T> valueType) {
        return fromJson(
            new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)),
            valueType
        );
    }

    /**
     * Serializes the provided objectValue into JSON format and returns it as a String. The objectValue can be any Java object that can
     * be represented in JSON format.
     *
     * @param objectValue the object to be serialized into JSON
     * @return a String containing the serialized JSON representation of the object
     */
    default String toJson(Object objectValue) {
        var outputStream = new java.io.ByteArrayOutputStream();
        toJson(objectValue, outputStream);
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    /**
     * Returns the default instance of JsonBinding. This instance is lazily loaded and can be overridden for testing purposes.
     *
     * @return the default instance of JsonBinding
     */
    static JsonBinding get() {
        return JsonBindingHolder.getInstance();
    }

    /**
     * Holder class for the default instance of JsonBinding. This class is responsible for lazy-loading the singleton instance and
     * providing methods to override or reset it for testing purposes.
     */
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

        private JsonBindingHolder() {
        }
    }

}
