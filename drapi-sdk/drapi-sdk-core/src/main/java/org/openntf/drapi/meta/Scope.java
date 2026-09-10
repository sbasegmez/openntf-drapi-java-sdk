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
package org.openntf.drapi.meta;

/**
 * This record is a Scope data schema. Don't confuse it with the DrapiDataSource interface, which is a representation of an API context
 * on DRAPI and incidentally referred to as Scope in the DRAPI interface. This record is used to define the structure of a scope,
 * including its name, NSF path, schema name, icon name, icon, description, and active status.
 *
 * @param apiName The name of the API associated with this scope.
 * @param nsfPath The NSF path associated with this scope.
 * @param schemaName The schema name associated with this scope.
 * @param iconName The name of the icon associated with this scope.
 * @param icon The icon associated with this scope.
 * @param description A description of this scope.
 * @param isActive A boolean indicating whether this scope is active.
 */
public record Scope(String apiName, String nsfPath, String schemaName, String iconName, String icon, String description,
                    boolean isActive) {

}
