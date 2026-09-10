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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface Document {

    /**
     * Returns the form name, read from the {@code Form} item, or from {@code @form} when the server sent it there. While Domino doesn't
     * require a "form" value, OpenAPI mandates a "form" field. Documents without a form field are only shown within lists. So the
     * Document implementation should always return a non-empty value.
     *
     * @return the form name
     */
    String form();

    /**
     * Server-supplied metadata, if any, or an empty optional. The metadata is returned in the "@meta" field of the document payload.
     *
     * @return the server-supplied metadata, absent for user-created document and when the payload doesn't return "@meta".
     */
    Optional<DocumentMeta> meta();

    /**
     * Server-supplied warnings, if any, or an empty list. The warnings are returned in the "@warnings" field of the document payload.
     *
     * @return the server-supplied warnings as "@warnings", if any, or an empty list
     */
    List<String> warnings();

    /**
     * Returns the names of the data fields in the document, excluding documented metadata keys.
     *
     * @return the data field names, in case-insensitive order and with the server's original casing, excluding documented metadata keys
     * (e.g. "@meta", "@form", "@warnings")
     */
    Set<String> fieldNames();

    /**
     * Create a stream of the fields, in case-insensitive order and with the server's original casing, excluding metadata keys
     *
     * @return the stream of fields
     */
    Stream<Field> fields();

    /**
     * Returns the field with the given name. The name lookup is case-insensitive.
     *
     * @param name the name of the field
     * @return the field with the given name
     */
    Field field(String name);

}
