package org.openntf.drapi.internal.meta;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.openntf.drapi.meta.Document;
import org.openntf.drapi.meta.DocumentMeta;
import org.openntf.drapi.meta.Field;
import org.openntf.drapi.util.TypeUtils;

public class DocumentImpl implements Document {

    private final String form;
    private final DocumentMeta meta;
    private final List<String> warnings;
    private final Map<String, Object> valueMap;

    /**
     * Constructs a DocumentImpl instance.
     * <p>
     * We only do shallow-copy for the valueMap, so the caller should not modify it after passing it to this constructor. Warnings will
     * not be copied. In theory, the caller should not modify it after passing it to this constructor. The meta object is immutable, so
     * no need to copy it.
     *
     * @param form     the form name of the document
     * @param meta     the metadata associated with the document, can be null
     * @param warnings a list of warnings related to the document, can be null
     * @param valueMap a map containing field names and their corresponding values
     */
    public DocumentImpl(String form, DocumentMeta meta, List<String> warnings, Map<String, Object> valueMap) {
        this.form = TypeUtils.requireNonEmpty(form, "Form name cannot be null or empty");
        this.meta = meta;
        this.warnings = warnings == null ? List.of() : warnings;

        this.valueMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.valueMap.putAll(Objects.requireNonNull(valueMap, "Value map cannot be null"));
    }

    @Override
    public String form() {
        return form;
    }

    @Override
    public Optional<DocumentMeta> meta() {
        return Optional.ofNullable(meta);
    }

    @Override
    public List<String> warnings() {
        return warnings;
    }

    @Override
    public Set<String> fieldNames() {
        return Collections.unmodifiableSet(valueMap.keySet());
    }

    @Override
    public Stream<Field> fields() {
        return valueMap.entrySet()
                       .stream()
                       .map(entry -> new FieldImpl(entry.getKey(), entry.getValue(), true));
    }

    @Override
    public Field field(String name) {
        Object value = valueMap.get(name);

        // value being null does not mean the field is absent, it could be present with a null value. So we check if the key exists in the map.
        return new FieldImpl(name, value, valueMap.containsKey(name));
    }
}
