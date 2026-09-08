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
package org.openntf.drapi.json.jakarta;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.openntf.drapi.exception.JsonBindingException;
import org.openntf.drapi.json.JsonBinding;

public class JakartaJsonBinding implements JsonBinding {

    private final Jsonb jsonb;

    JakartaJsonBinding() {
        this.jsonb = JsonbBuilder.create(
            new JsonbConfig().withNullValues(true) // Include null values in the output
        );
    }

    @Override
    public String name() {
        return "Jakarta";
    }

    @Override
    public Map<String, Object> fromJson(InputStream jsonStream) {
        Objects.requireNonNull(jsonStream, "jsonStream cannot be null");

        try (JsonReader reader = Json.createReader(jsonStream)) {
            JsonValue value = reader.readValue();
            if (!(value instanceof JsonObject object)) {
                throw new JsonBindingException("Expected a JSON object but found " + value.getValueType());
            }
            return toMap(object);
        } catch (JsonException e) {
            throw new JsonBindingException("Could not parse JSON object", e);
        }
    }

    @Override
    public <T> T fromJson(InputStream jsonStream, Class<T> valueType) {
        Objects.requireNonNull(jsonStream, "jsonStream cannot be null");
        Objects.requireNonNull(valueType, "valueType cannot be null");

        try {
            return jsonb.fromJson(jsonStream, valueType);
        } catch (Exception e) {
            throw new JsonBindingException("Could not parse JSON", e);
        }
    }

    @Override
    public void toJson(Object objectValue, OutputStream outputStream) {
        Objects.requireNonNull(objectValue, "objectValue cannot be null");
        Objects.requireNonNull(outputStream, "outputStream cannot be null");

        try {
            jsonb.toJson(objectValue, outputStream);
        } catch (Exception e) {
            throw new JsonBindingException("Could not convert object to JSON", e);
        }
    }

    @Override
    public Stream<Map<String, Object>> streamFromJsonArray(InputStream jsonStream) {
        final JsonParser parser;

        // Let's try to create the parser first, and if it fails, we can close the stream manually.
        try {
            parser = Json.createParser(Objects.requireNonNull(jsonStream, "jsonStream cannot be null"));
        } catch (JsonException e) {
            // We don't have a parser to close here, so we need to close stream manually.
            try {
                jsonStream.close();
            } catch (Exception closeException) {
                e.addSuppressed(closeException);
            }
            throw new JsonBindingException("Could not create JSON parser", e);
        }

        // Now we have a parser, we don't have to think about the stream anymore.
        boolean parserNeedsClosing = true; // Flag to track if the parser needs to be closed

        try {
            if (!parser.hasNext()) {
                throw new JsonBindingException("Empty JSON stream");
            }

            Event firstEvent = parser.next(); // Move to the first event

            if (firstEvent == Event.START_ARRAY) {

                parserNeedsClosing = false; // The parser will be closed by the stream's onClose handler

                // TODO This code fails with a JsonException if the input stream ended prematurely. We can create our own stream
                //  from a spliterator that reads elements one by one and handles premature end of stream gracefully.
                //  Manual stream might also eliminate the need for the onClose handler.

                return parser.getArrayStream()
                             .map(JakartaJsonBinding::requireObject) // Ensure each element is a JsonObject
                             .map(JakartaJsonBinding::toMap)
                             .onClose(parser::close); // Ensure the parser is closed when the stream is closed

            } else {
                throw new JsonBindingException("Expected a JSON array but found " + firstEvent);
            }

        } catch (JsonException e) {
            throw new JsonBindingException("Could not parse JSON array", e);
        } finally {
            if (parserNeedsClosing) {
                parser.close(); // Close the parser if it wasn't handed over to the stream's onClose handler
            }
        }
    }

    private static JsonObject requireObject(JsonValue jsonValue) {
        if (!(jsonValue instanceof JsonObject object)) {
            throw new JsonBindingException("Expected a JSON object but found " + jsonValue.getValueType());
        }
        return object;
    }

    private static Map<String, Object> toMap(JsonObject jsonObject) {
        // Use LinkedHashMap to preserve insertion order
        Map<String, Object> map = new LinkedHashMap<>();
        jsonObject.forEach((key, value) -> map.put(key, toJavaValue(value)));
        return map;
    }

    private static List<Object> toList(JsonArray jsonArray) {
        // Use ArrayList to preserve insertion order
        List<Object> values = new ArrayList<>(jsonArray.size());
        jsonArray.forEach(element -> values.add(toJavaValue(element)));
        return values;
    }

    private static Object toJavaValue(JsonValue value) {
        return switch (value.getValueType()) {
            case NULL -> null;
            case STRING -> ((JsonString) value).getString();
            case NUMBER -> toNumber((JsonNumber) value);
            case TRUE -> Boolean.TRUE;
            case FALSE -> Boolean.FALSE;
            case OBJECT -> toMap((JsonObject) value);
            case ARRAY -> toList((JsonArray) value);
        };
    }

    /**
     * Converts a JsonNumber to a Java Number (Long or Double) based on its value. This method ensures that integral numbers are
     * represented as Long, while non-integral numbers are represented as Double.
     * <p>
     * JsonNumber.toNumber() returns a Number, but we don't want to involve BigDecimal or BigInteger, so we check if the number is
     * integral and return a Long or Double accordingly.
     *
     * @param number the JsonNumber to convert
     * @return a Long if the number is integral, otherwise a Double
     */
    private static Object toNumber(JsonNumber number) {
        if (number.isIntegral()) {
            return number.longValue();
        } else {
            return number.doubleValue();
        }
    }

}
