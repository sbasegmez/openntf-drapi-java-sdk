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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.openntf.drapi.meta.Column;
import org.openntf.drapi.meta.ListEntry;

public class ListEntryImpl implements ListEntry {

    private final String unid;
    private final Integer noteId;
    private final String index;
    private final Boolean unread;

    // Implementation mandates using a TreeMap with case-insensitive ordering for field names. This ensures that field access is case-insensitive.
    private final TreeMap<String, Object> valueMap;

    ListEntryImpl(String unid, Integer noteId, String index, Boolean unread, Map<String, Object> valueMap) {
        this.unid = unid;
        this.noteId = noteId;
        this.index = index;
        this.unread = unread;

        this.valueMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.valueMap.putAll(Objects.requireNonNull(valueMap, "Value map cannot be null"));
    }

    @Override
    public Optional<String> unid() {
        return Optional.ofNullable(unid);
    }

    @Override
    public OptionalInt noteId() {
        return noteId == null ? OptionalInt.empty() : OptionalInt.of(noteId);
    }

    @Override
    public Optional<String> index() {
        return Optional.ofNullable(index);
    }

    @Override
    public Optional<Boolean> unread() {
        return Optional.ofNullable(unread);
    }

    @Override
    public Set<String> columnNames() {
        return Collections.unmodifiableSet(valueMap.keySet());
    }

    @Override
    public Stream<Column> columns() {
        return valueMap.entrySet()
            .stream()
            .map(entry -> new ColumnImpl(entry.getKey(), entry.getValue(), true));
    }

    @Override
    public Column column(String name) {
        Objects.requireNonNull(name, "Column name cannot be null");

        Object value = valueMap.get(name);
        boolean exists = valueMap.containsKey(name);

        // If the column is present, we return the original case of the column name as stored in the valueMap. If not present, we return the requested name.
        String actualName = valueMap.containsKey(name) ? valueMap.ceilingKey(name) : name;

        return new ColumnImpl(actualName, value, exists);
    }
}
