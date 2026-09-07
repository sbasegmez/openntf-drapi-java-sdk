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
package org.openntf.drapi.internal.meta;

import java.util.List;
import java.util.Objects;
import org.openntf.drapi.exception.DrapiException;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.json.JsonBinding;
import org.openntf.drapi.meta.Document;
import org.openntf.drapi.meta.DocumentMeta;
import org.openntf.drapi.util.TypeUtils;

public class ResponseParser {

    private ResponseParser() {
        // Private constructor to prevent instantiation
    }

    public static Document toDocument(DrapiResponse response) {
        Objects.requireNonNull(response, "Response cannot be null");

        JsonBinding jsonBinding = JsonBinding.get();

        // Might throw JsonParsingException if the response body is not valid JSON
        var bodyTree = jsonBinding.fromJson(response.bodyAsString());

        DocumentMeta meta = null;
        List<String> warnings = List.of();
        String form = null;

        if (bodyTree.containsKey("@meta")) {
            meta = toDocumentMeta(bodyTree.get("@meta"));
            bodyTree.remove("@meta");
        }

        if (bodyTree.get("@warnings") instanceof List<?> list) {
            warnings = list.stream().map(Object::toString).toList();
            bodyTree.remove("@warnings");
        }

        // TODO Investigate in what cases @form is present and in what cases Form is present.
        // For now, we will check for @form first, and if it's not present, we will check for Form.
        // We expect that the "Form" field is always present in the bodyTree, but if it's not, that's an invalid response.
        // We don't remove the "Form" field from the bodyTree, as it is part of the document's data.
        if (bodyTree.containsKey("@form")) {
            form = bodyTree.get("@form").toString();
            bodyTree.remove("@form");
        } else if (bodyTree.containsKey("Form")) {
            // "Form" field name is always be "Form", not "form" or "FORM", so we don't need to check for case-insensitive match.
            form = bodyTree.get("Form").toString();
        }

        if(TypeUtils.isEmpty(form)) {
            throw new DrapiException("Form field is missing in the response body. This is an invalid response.", null, response);
        }

        return new DocumentImpl(
            form,
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
