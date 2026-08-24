package org.openntf.json.jakarta;

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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        Objects.requireNonNull(jsonStream, "Input stream cannot be null");

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
        Objects.requireNonNull(jsonStream, "Input stream cannot be null");
        Objects.requireNonNull(valueType, "Value type cannot be null");
        try {
            return jsonb.fromJson(jsonStream, valueType);
        } catch (Exception e) {
            throw new JsonBindingException("Could not parse JSON", e);
        }
    }

    @Override
    public void toJson(Object objectValue, OutputStream outputStream) {
        Objects.requireNonNull(objectValue, "Object value cannot be null");
        Objects.requireNonNull(outputStream, "Output stream cannot be null");
        try {
            jsonb.toJson(objectValue, outputStream);
        } catch (Exception e) {
            throw new JsonBindingException("Could not convert object to JSON", e);
        }
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
        if(number.isIntegral()) {
            return number.longValue();
        } else {
            return number.doubleValue();
        }
    }

}
