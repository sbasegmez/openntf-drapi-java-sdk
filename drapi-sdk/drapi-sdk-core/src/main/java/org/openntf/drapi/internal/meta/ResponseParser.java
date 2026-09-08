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
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.openntf.drapi.exception.DrapiException;
import org.openntf.drapi.exception.JsonBindingException;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.json.JsonBinding;
import org.openntf.drapi.meta.Document;
import org.openntf.drapi.meta.DocumentMeta;
import org.openntf.drapi.meta.ListEntry;
import org.openntf.drapi.util.TypeUtils;

public class ResponseParser {

    public static final String FIELD_FORM = "Form";

    public static final String AT_FORM = "@form";
    public static final String AT_META = "@meta";
    public static final String AT_WARNINGS = "@warnings";
    public static final String AT_UNID = "@unid";
    public static final String AT_NOTEID = "@noteid";
    public static final String AT_INDEX = "@index";
    public static final String AT_UNREAD = "@unread";


    // Field names within the "@meta" object (see the DocumentMeta schema of the Domino REST API basis OpenAPI spec, version 1.42.6).
    public static final String META_NOTEID = "noteid";
    public static final String META_UNID = "unid";
    public static final String META_CREATED = "created";
    public static final String META_ADDED_TO_FILE = "addedtofile";
    public static final String META_LAST_MODIFIED = "lastmodified";
    public static final String META_LAST_MODIFIED_IN_FILE = "lastmodifiedinfile";
    public static final String META_LAST_ACCESSED = "lastaccessed";
    public static final String META_NOTE_CLASS = "noteclass";
    public static final String META_UNREAD = "unread";
    public static final String META_EDITABLE = "editable";
    public static final String META_REVISION = "revision";
    public static final String META_ETAG = "etag";
    public static final String META_TOP_LEVEL_CHILD_UNIDS = "toplevelchildunids";
    public static final String META_SIZE = "size";
    public static final String META_FORM = "form";

    private ResponseParser() {
        // Private constructor to prevent instantiation
    }

    public static Document toDocument(DrapiResponse response) {
        Objects.requireNonNull(response, "Response cannot be null");

        JsonBinding jsonBinding = JsonBinding.get();
        Map<String, Object> bodyTree;

        try (response) {
            // Might throw JsonParsingException if the response body is not valid JSON
            bodyTree = jsonBinding.fromJson(response.bodyAsString());
        } catch (JsonBindingException e) {
            throw new DrapiException("Failed to parse response body as JSON", null, response, e);
        }

        DocumentMeta meta = null;
        List<String> warnings = List.of();
        String form = null;

        if (bodyTree.containsKey(AT_META)) {
            meta = toDocumentMeta(bodyTree.get(AT_META));
            bodyTree.remove(AT_META);
        }

        if (bodyTree.get(AT_WARNINGS) instanceof List<?> list) {
            warnings = list.stream().map(Object::toString).toList();
            bodyTree.remove(AT_WARNINGS);
        }

        // TODO Investigate in what cases @form is present and in what cases Form is present.
        // For now, we will check for @form first, and if it's not present, we will check for Form.
        // We expect that the "Form" field is always present in the bodyTree, but if it's not, that's an invalid response.
        // We don't remove the "Form" field from the bodyTree, as it is part of the document's data.
        if (TypeUtils.isNotEmpty(bodyTree.get(AT_FORM))) {
            form = bodyTree.get(AT_FORM).toString();
            bodyTree.remove(AT_FORM);
        } else if (TypeUtils.isNotEmpty(bodyTree.get(FIELD_FORM))) {
            // "Form" field name will always be "Form", not "form" or "FORM", so we don't need to check for case-insensitive match.
            form = bodyTree.get(FIELD_FORM).toString();
        }

        if (TypeUtils.isEmpty(form)) {
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
        if (obj instanceof java.util.Map<?, ?> map) {
            return new DocumentMeta(
                DataTypeUtils.toLong(map.get(META_NOTEID)).orElse(null),
                DataTypeUtils.toStringValue(map.get(META_UNID)).orElse(null),
                DataTypeUtils.toDateTime(map.get(META_CREATED)).orElse(null),
                DataTypeUtils.toDateTime(map.get(META_ADDED_TO_FILE)).orElse(null),
                DataTypeUtils.toDateTime(map.get(META_LAST_MODIFIED)).orElse(null),
                DataTypeUtils.toDateTime(map.get(META_LAST_MODIFIED_IN_FILE)).orElse(null),
                DataTypeUtils.toDateTime(map.get(META_LAST_ACCESSED)).orElse(null),
                DataTypeUtils.typedList(map.get(META_NOTE_CLASS), String.class).orElse(List.of()),
                DataTypeUtils.toBoolean(map.get(META_UNREAD)).orElse(null),
                DataTypeUtils.toBoolean(map.get(META_EDITABLE)).orElse(null),
                DataTypeUtils.toStringValue(map.get(META_REVISION)).orElse(null),
                DataTypeUtils.toStringValue(map.get(META_ETAG)).orElse(null),
                DataTypeUtils.typedList(map.get(META_TOP_LEVEL_CHILD_UNIDS), String.class).orElse(List.of()),
                DataTypeUtils.toLong(map.get(META_SIZE)).orElse(null),
                DataTypeUtils.toStringValue(map.get(META_FORM)).orElse(null)
            );
        }

        return null;
    }

    /**
     * Converts a response body tree to a stream of ListEntry objects.
     * <p>
     * This method expects the JSON response body to contain an array of entries in a format that can be converted to ListEntry
     * objects.
     *
     * @param response The DrapiResponse containing the JSON body to be converted.
     * @return A Stream of ListEntry objects constructed from the response body.
     */
    public static Stream<ListEntry> toListEntryStream(DrapiResponse response) {
        Objects.requireNonNull(response, "Response cannot be null");

        JsonBinding jsonBinding = JsonBinding.get();

        try (response) {
            return jsonBinding.streamFromJsonArray(response.bodyStream())
                              .map(ResponseParser::toListEntry)
                              .onClose(response::close);  // Ensure the response is closed when the stream is closed
        }
    }

    /**
     * Converts a response body tree to a ListEntry object.
     * <p>
     * This method expects the simple map representation of a ListEntry.
     *
     * @param valueMap The map representation of the ListEntry.
     * @return A ListEntry object constructed from the provided map.
     */
    public static ListEntry toListEntry(Map<String, Object> valueMap) {
        Objects.requireNonNull(valueMap, "Value map cannot be null");

        String unid = null;
        Integer noteId = null;
        String index = null;
        Boolean unread = null;

        // Create a copy to avoid modifying the original map
        Map<String, Object> valueMapCopy = new TreeMap<>(valueMap);

        if (valueMapCopy.containsKey(AT_UNID)) {
            unid = DataTypeUtils.toStringValue(valueMapCopy.get(AT_UNID)).orElse(null);
            valueMapCopy.remove(AT_UNID);
        }

        if (valueMapCopy.containsKey(AT_NOTEID)) {
            noteId = DataTypeUtils.toInteger(valueMapCopy.get(AT_NOTEID)).orElse(null);
            valueMapCopy.remove(AT_NOTEID);
        }

        if (valueMapCopy.containsKey(AT_INDEX)) {
            index = DataTypeUtils.toStringValue(valueMapCopy.get(AT_INDEX)).orElse(null);
            valueMapCopy.remove(AT_INDEX);
        }

        if (valueMapCopy.containsKey(AT_UNREAD)) {
            unread = DataTypeUtils.toBoolean(valueMapCopy.get(AT_UNREAD)).orElse(null);
            valueMapCopy.remove(AT_UNREAD);
        }

        return new ListEntryImpl(unid, noteId, index, unread, valueMapCopy);
    }
}
