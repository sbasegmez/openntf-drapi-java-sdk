package org.openntf.drapi.internal.meta;

import java.util.List;
import java.util.Objects;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.json.JsonBinding;
import org.openntf.drapi.meta.Document;
import org.openntf.drapi.meta.DocumentMeta;

public class ResponseParser {

    public static Document toDocument(DrapiResponse response) {
        Objects.requireNonNull(response, "Response cannot be null");

        JsonBinding jsonBinding = JsonBinding.get();

        // Might throw JsonParsingException if the response body is not valid JSON
        var bodyTree = jsonBinding.fromJson(response.bodyAsString());

        DocumentMeta meta = null;
        List<String> warnings = null;

        if(bodyTree.containsKey("@meta")) {
            meta = toDocumentMeta(bodyTree.get("@meta"));
            bodyTree.remove("@meta");
        }

        if(bodyTree.get("@warnings") instanceof List<?> list) {
            warnings = list.stream().map(Object::toString).toList();
            bodyTree.remove("@warnings");
        }

        return new DocumentImpl(
            bodyTree.getOrDefault("Form", "").toString(),
            meta,
            warnings,
            bodyTree
        );
    }

    private static DocumentMeta toDocumentMeta(Object obj) {
        if(obj instanceof java.util.Map<?, ?> map) {
            return new DocumentMeta(
                DataTypeUtils.toLong(map.get("noteid")).orElse(null),
                DataTypeUtils.toString(map.get("unid")).orElse(null),
                DataTypeUtils.toDateTime(map.get("created")).orElse(null),
                DataTypeUtils.toDateTime(map.get("addedtofile")).orElse(null),
                DataTypeUtils.toDateTime(map.get("lastmodified")).orElse(null),
                DataTypeUtils.toDateTime(map.get("lastmodifiedinfile")).orElse(null),
                DataTypeUtils.toDateTime(map.get("lastaccessed")).orElse(null),
                DataTypeUtils.typedList(map.get("noteclass"), String.class).orElse(List.of()),
                DataTypeUtils.toBoolean(map.get("unread")).orElse(null),
                DataTypeUtils.toBoolean(map.get("editable")).orElse(null),
                DataTypeUtils.toString(map.get("revision")).orElse(null),
                DataTypeUtils.toString(map.get("etag")).orElse(null),
                DataTypeUtils.typedList(map.get("toplevelchildunids"), String.class).orElse(List.of()),
                DataTypeUtils.toLong(map.get("size")).orElse(null),
                DataTypeUtils.toString(map.get("form")).orElse(null)
            );


        }

        return null;
    }

}
