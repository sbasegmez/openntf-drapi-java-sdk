package org.openntf.drapi.auth;

import org.openntf.drapi.auth.builtin.SingleUserSessionContext;

/**
 * Represents the context of a user session in the DRAPI SDK. This interface provides methods to retrieve session-related information
 * such as the session ID and username. The values returned by these methods are primarily intended for logging and debugging purposes,
 * and the SDK does not interpret them. This class will not be used for indexing or keying. This is sort of a placeholder for
 * implementations to provide session context information.
 * <p>
 * The implementation of this interface should ensure that the session ID and username are treated as credentials and handled securely.
 * <p>
 * Also, the session context SHOULD NOT keep a live container object, and must stay stable for the lifetime of the user session. The
 * SessionContext objects are created on one thread and continue on other threads. So they should not use ThreadLocal or similar
 * constructs to store state.
 */
public interface SessionContext {

    /**
     * Returns the session ID associated with the current session context. The value should be treated as a credential.
     * <p>
     * The value is held purely for logging/debugging purposes and The SDK does not interpret it.
     *
     * @return any string value that can be used to identify the session in logs/debugging
     */
    String sessionId();

    /**
     * Returns the username associated with the current session context.
     * <p>
     * SDK does not interpret it, but it can be used for logging/debugging purposes.
     *
     * @return any string value that can be used to identify the user in logs/debugging
     */
    String username();

    /**
     * Returns a SessionContext implementation suitable for single-user scenarios. This implementation generates a unique session ID
     * upon instantiation and provides a fixed username. It is intended for use in applications where there is only one user session,
     * and the session context does not need to be dynamic or multi-threaded.
     *
     * @return a SessionContext implementation suitable for single-user scenarios
     */
    static SessionContext singleUser() {
        return new SingleUserSessionContext();
    }

}
