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
import org.openntf.drapi.util.TypeUtils;

/**
 * This exception is thrown when authentication fails, such as when the username or password is incorrect.
 *
 * It's a subclass of {@link DrapiException}, which is the base exception class for all exceptions in the Drapi SDK.
 */
public class AuthenticationException extends DrapiException {

    /**
     * This exception is thrown when authentication fails, such as when the username or password is incorrect.
     *
     * @param message  the error message
     * @param request  the DrapiRequest that caused the exception
     * @param response the DrapiResponse that caused the exception
     */
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
