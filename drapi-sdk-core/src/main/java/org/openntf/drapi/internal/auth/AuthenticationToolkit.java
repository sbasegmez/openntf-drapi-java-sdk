package org.openntf.drapi.internal.auth;

import java.util.Objects;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.json.JsonBinding;

/**
 * A contextual toolkit for authentication operations.
 * <p>
 * This record encapsulates the necessary components for performing authentication, including an HttpTransport for making HTTP requests
 * and a JsonBinding for handling JSON serialization and deserialization. This is to be injected where needed.
 */
public record AuthenticationToolkit(HttpTransport httpTransport, JsonBinding jsonBinding) {

    /**
     * Creates a new instance of AuthenticationToolkit with the specified HttpTransport.
     *
     * @param httpTransport the bare (non-authenticating) HttpTransport to be used for authentication operations.
     * @param jsonBinding   the JsonBinding to be used for JSON processing during authentication
     */
    public AuthenticationToolkit {
        Objects.requireNonNull(httpTransport, "httpTransport must not be null");
        Objects.requireNonNull(jsonBinding, "jsonBinding must not be null");
    }

}
