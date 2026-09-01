package org.openntf.drapi.exception;

import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.http.HttpMethod;

/**
 * DrapiException is a custom exception class that extends RuntimeException.
 * It is used to represent exceptions that occur during the execution of the Drapi SDK.
 * This exception class captures relevant information about the HTTP request and response
 * that led to the exception, including the HTTP method, request path, status code, and error message.
 */
public class DrapiException extends RuntimeException {

    private final HttpMethod httpMethod;
    private final String path;
    private final int statusCode;
    private final ErrorMessage errorMessage;

    public DrapiException(String message, DrapiRequest request, DrapiResponse response, Throwable cause) {
        super(message, cause);

        this.httpMethod = request == null ? null : request.httpMethod();
        this.path = request == null ? null : request.path();
        this.statusCode = response == null ? 0 : response.statusCode();

        ErrorMessage tmpErrorMsg = null;

        if(response != null && !response.isSuccess()) {
            try {
                tmpErrorMsg = ErrorMessage.fromJson(response.bodyAsString());
            } catch (Exception e) {
                // Swallow this exception, as we don't want to throw an exception while constructing another exception.
            }
        }

        this.errorMessage = tmpErrorMsg;
    }

    public DrapiException(String message, DrapiRequest request, DrapiResponse response) {
        this(message, request, response, null);
    }

    public DrapiException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    public DrapiException(String message) {
        this(message, null, null, null);
    }

    /* --- We switch to getter style to conform to Exception's own methods --- */

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorMessageString() {
        return errorMessage == null ? null : errorMessage.message();
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + (getErrorMessageString() == null ? "" : " [" + getErrorMessageString() + "]");
    }
}
