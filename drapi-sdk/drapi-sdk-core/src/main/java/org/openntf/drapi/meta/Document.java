package org.openntf.drapi.meta;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface Document {

    /**
     * Returns the form name, read from the {@code Form} item, or from {@code @form} when the server sent it there. While Domino doesn't
     * required a "form" value, it is marked as required in the OpenAPI specs. So implementation should always return a non-empty
     * value.
     *
     * @return the form name
     */
    String form();

    /**
     * @return the server-supplied metadata, absent for user-created document and when the payload doesn't return "@meta".
     */
    Optional<DocumentMeta> meta();

    /**
     * @return the server-supplied warnings as "@warnings", if any, or an empty list
     */
    List<String> warnings();

    /**
     * @return the data field names, in case-insensitive order and with the server's original casing, excluding metadata keys
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
     * @param name the name of the field
     * @return the field with the given name
     */
    Field field(String name);

}
