package org.openntf.drapi.auth.builtin;

import java.util.UUID;
import org.openntf.drapi.auth.SessionContext;

public class PasswordSessionContext implements SessionContext {

    // Session ID is generated once per instance and remains constant for the lifetime of the session context.
    private final String sessionId = UUID.randomUUID().toString();

    @Override
    public String sessionId() {
        return sessionId;
    }

    @Override
    public String username() {
        return "LocalUser"; // Placeholder username; in a real implementation, this would be dynamic.
    }
}
