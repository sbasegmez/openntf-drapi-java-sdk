package org.openntf.drapi.internal.auth;

import java.util.Objects;
import org.openntf.drapi.http.HttpTransport;

/**
 * A contextual toolkit for authentication operations.
 * <p>
 * This record encapsulates the necessary components for performing authentication, including an HttpTransport for making HTTP requests.
 * This class has only one component for now, but it is designed to be extensible in the future to include additional components as needed.
 */
public record AuthenticationToolkit(HttpTransport httpTransport) {

    /**
     * Creates a new instance of AuthenticationToolkit with the specified HttpTransport.
     *
     * @param httpTransport the bare (non-authenticating) HttpTransport to be used for authentication operations.
     */
    public AuthenticationToolkit {
        Objects.requireNonNull(httpTransport, "httpTransport must not be null");
    }

}
