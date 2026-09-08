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
package org.openntf.drapi.meta;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Stream;

public interface ListEntry {

    /**
     * Returns the universal ID of the documents which belongs to the entry, if available.
     * <p>
     * Metadata keys may not be available when they are not requested in the request.
     *
     * @return Optional containing universal id (marked as @unid in the response), or empty if not available
     */
    Optional<String> unid();

    /**
     * Returns the note ID of the documents which belongs to the entry, if available.
     * <p>
     * Metadata keys may not be available when they are not requested in the request.
     *
     * @return OptionalInt containing the note ID (marked as @noteid in the response), or empty if not available
     */
    OptionalInt noteId();

    /**
     * Returns the index of the documents which belongs to the entry, if available. Index is the position of the entry in the view,
     * and it reflects the indentation level of the entry in the view hierarchy.
     * <p>
     * Metadata keys may not be available when they are not requested in the request.
     *
     * @return Optional containing the index (marked as @index in the response), or empty if not available
     */
    Optional<String> index();

    /**
     * Returns whether the documents which belongs to the entry is unread, if available.
     * <p>
     * Metadata keys may not be available when they are not requested in the request.
     *
     * @return Optional containing whether the entry is unread (marked as @unread in the response), or empty if not available
     */
    Optional<Boolean> unread();

    /**
     * @return the column names, in case-insensitive order and with the server's original casing, excluding metadata keys
     */
    Set<String> columnNames();

    /**
     * Create a stream of the columns, in case-insensitive order and with the server's original casing, excluding metadata keys
     *
     * @return the stream of columns
     */
    Stream<Column> columns();

    /**
     * Returns the column with the given name. The name lookup is case-insensitive.
     *
     * @param name the name of the column
     * @return the column with the given name
     */
    Column column(String name);

}
