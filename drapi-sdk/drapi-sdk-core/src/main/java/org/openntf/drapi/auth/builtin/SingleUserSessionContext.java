package org.openntf.drapi.auth.builtin;

import java.util.UUID;
import org.openntf.drapi.auth.SessionContext;

/**
 * A simple implementation of SessionContext for single-user scenarios.
 * This class generates a unique session ID upon instantiation and provides a fixed username.
 */
public class SingleUserSessionContext implements SessionContext {

    // Session ID is generated once per instance and remains constant for the lifetime of the session context.
    private final String sessionId = UUID.randomUUID().toString();

    @Override
    public String sessionId() {
        return sessionId;
    }

    @Override
    public String username() {
        return "single-user"; // Placeholder username; in a real implementation, this would be dynamic.
    }
}
