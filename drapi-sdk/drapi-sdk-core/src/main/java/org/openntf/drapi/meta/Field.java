package org.openntf.drapi.meta;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * This interface provides fluent access (like Helidon Config API) to document fields, allowing for type-safe retrieval of field values
 * in various formats. It supports checking for presence, nullability, and multi-value status of fields, as well as retrieving raw
 * values and converting them to specific types.
 */
public interface Field {

    /**
     * Returns the name of the field, preserving the original casing as received from the server.
     *
     * @return the field name
     */
    String name();

    /**
     * Checks whether the field is present in the document, regardless of its value.
     *
     * @return true if the field is present, false otherwise
     */
    boolean isPresent();

    /**
     * Checks whether the field value is null.
     *
     * @return true if the field value is null, false otherwise
     */
    boolean isNull();

    /**
     * Checks whether the field contains multiple values.
     *
     * @return true if the field is multi-valued, false otherwise
     */
    boolean isMultiValue();

    /**
     * Returns the raw value of the field, if present.
     *
     * @return an Optional containing the raw value, or empty if not present
     */
    Optional<Object> raw();

    /**
     * Returns the field value as the specified type.
     *
     * @param type the class of the type to convert the field value to
     * @param <T>  the type to convert the field value to
     * @return an Optional containing the representation of the field value as the specified type
     */
    <T> Optional<T> as(Class<T> type);        // the general form; the above are shortcuts

    /**
     * Returns the field value as a list of the specified type.
     *
     * @param type the class of the type to convert the field values to
     * @param <T>  the type to convert the field values to
     * @return a List containing the representation of the field values as the specified type
     */
    <T> List<T> asList(Class<T> type);

    /**
     * Returns the field value as a String.
     *
     * @return an Optional containing the String representation of the field value
     */
    Optional<String> asString();

    /**
     * Returns the field value as an Integer.
     *
     * @return an Optional containing the Integer representation of the field value
     */
    Optional<Integer> asInt();

    /**
     * Returns the field value as a Long.
     *
     * @return an Optional containing the Long representation of the field value
     */
    Optional<Long> asLong();

    /**
     * Returns the field value as a Double.
     *
     * @return an Optional containing the Double representation of the field value
     */
    Optional<Double> asDouble();

    /**
     * Returns the field value as a Boolean.
     *
     * @return an Optional containing the Boolean representation of the field value
     */
    Optional<Boolean> asBoolean();

    /**
     * Returns the field value as an OffsetDateTime.
     *
     * @return an Optional containing the OffsetDateTime representation of the field value
     */
    Optional<OffsetDateTime> asDateTime();

    /**
     * Returns the field value as a LocalDate.
     *
     * @return an Optional containing the LocalDate representation of the field value
     */
    Optional<LocalDate> asDate();

}
