package org.openntf.drapi.exception;

import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.util.TypeUtils;

public class AuthenticationException extends DrapiException {

    // This exception is thrown when authentication fails, such as when the username or password is incorrect.
    public AuthenticationException(String message, DrapiRequest request, DrapiResponse response) {
        super(message, request, response);
    }

    /**
     * Authentication exceptions should have a specific error message indicating that authentication failed. If it doesn't exist, we
     * need to specify this in the error message string.
     *
     * @return the error message string
     */
    @Override
    public String getErrorMessageString() {
        return TypeUtils.defaultIfEmpty(super.getErrorMessageString(), "Unknown Error");
    }
}
