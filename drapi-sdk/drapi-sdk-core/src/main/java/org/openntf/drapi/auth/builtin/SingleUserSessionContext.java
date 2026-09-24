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
