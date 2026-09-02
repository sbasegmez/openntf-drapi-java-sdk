package org.openntf.drapi.exception;

import org.openntf.drapi.json.JsonBinding;

/**
 * Standard error message for Domino REST API responses.
 *
 * @param status the HTTP status code
 * @param message the error message
 * @param details additional details about the error
 * @param errorId the unique error identifier (Undocumented, maps to the error message)
 */
public record ErrorMessage(
    int status,
    String message,
    String details,
    int errorId) {

    public static ErrorMessage fromJson(String json) {
        return JsonBinding.get().fromJson(json, ErrorMessage.class);
    }

}
