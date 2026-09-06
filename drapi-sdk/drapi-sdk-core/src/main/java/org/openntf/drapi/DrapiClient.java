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
package org.openntf.drapi;

/**
 * The DrapiClient interface provides the gateway to access and interact with the DRAPI (Domino REST Api) system.
 */
public interface DrapiClient {

    /**
     * Retrieves a DrapiDataSource instance for the specified data source name. This provides gateway to all resources accessed by a
     * "dataSource" (apiName/scope) parameter within the DRAPI system and allows for further interactions with that data source.
     *
     * @param name the name of the data source
     * @return a DrapiDataSource instance representing the specified data source
     */
    DrapiDataSource dataSource(String name);

    static DrapiClientBuilder builder(DrapiConfig config) {
        return new DrapiClientBuilder(config);
    }

}
