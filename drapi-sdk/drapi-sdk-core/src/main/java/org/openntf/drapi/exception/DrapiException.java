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

    /**
     * Constructs a new DrapiException with the specified message, request, response, and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param request the DrapiRequest that caused the exception
     * @param response the DrapiResponse that caused the exception
     * @param cause the underlying cause of the exception (which is saved for later retrieval by the getCause() method)
     */
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

    /**
     * Constructs a new DrapiException with the specified message, request, and response.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param request the DrapiRequest that caused the exception
     * @param response the DrapiResponse that caused the exception
     */
    public DrapiException(String message, DrapiRequest request, DrapiResponse response) {
        this(message, request, response, null);
    }

    /**
     * Constructs a new DrapiException with the specified message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param cause the underlying cause of the exception (which is saved for later retrieval by the getCause() method)
     */
    public DrapiException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    /**
     * Constructs a new DrapiException with the specified message.
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public DrapiException(String message) {
        this(message, null, null, null);
    }

    /* --- We switch to getter style to conform to Exception's own methods --- */

    /**
     * Returns the HTTP method of the request that caused the exception.
     * @return the HTTP method of the request that caused the exception
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Returns the path of the request that caused the exception.
     *
     * @return the path of the request that caused the exception
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the HTTP status code of the response that caused the exception.
     *
     * @return the HTTP status code of the response that caused the exception
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the error message string from the response that caused the exception.
     * If the error message is not available, it returns null.
     *
     * @return the error message string from the response that caused the exception, or null if not available
     */
    public String getErrorMessageString() {
        return errorMessage == null ? null : errorMessage.message();
    }

    /**
     * Returns the ErrorMessage object from the response that caused the exception.
     * If the error message is not available, it returns null.
     *
     * @return the ErrorMessage object from the response that caused the exception, or null if not available
     */
    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns the detail message string of this exception, including the error message string if available.
     *
     * @return the detail message string of this exception, including the error message string if available
     */
    @Override
    public String getMessage() {
        return super.getMessage() + (getErrorMessageString() == null ? "" : " [" + getErrorMessageString() + "]");
    }
}
