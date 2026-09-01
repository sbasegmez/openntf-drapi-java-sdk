package org.openntf.drapi.exception;

public class HttpTransportException extends RuntimeException {

    public HttpTransportException(String message) {
        super(message);
    }

    public HttpTransportException(String message, Throwable cause) {
        super(message, cause);
    }

}
