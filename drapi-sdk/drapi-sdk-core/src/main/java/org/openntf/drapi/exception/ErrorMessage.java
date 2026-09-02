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

import org.openntf.drapi.json.JsonBinding;

/**
 * Standard error message for Domino REST API responses.
 *
 * @param status the HTTP status code
 * @param message the error message
 * @param details additional details about the error
 * @param errorId the unique error identifier (Undocumented, maps to the error message)
 */
public record ErrorMessage(
    int status,
    String message,
    String details,
    int errorId) {

    public static ErrorMessage fromJson(String json) {
        return JsonBinding.get().fromJson(json, ErrorMessage.class);
    }

}
