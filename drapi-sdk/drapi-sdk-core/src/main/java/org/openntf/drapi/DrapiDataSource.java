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

import org.openntf.drapi.api.DocumentsApi;

/**
 * This class represents an API context on DRAPI. It is the bridge between DrapiClient and the API functions.
 */
public interface DrapiDataSource {

    /**
     * The name of the data source representation. Also known as the "scope" or "apiName" in some contexts. This is the name that will
     * be used to identify the data source in the API.
     *
     * @return the name of the scope representation
     */
    String dataSource();

    /**
     * Provides access to the Documents API for this data source. This allows for operations related to documents within the specified
     * data source.
     *
     * @return an instance of DocumentsApi for interacting with documents in this data source
     */
    DocumentsApi documents();

}
