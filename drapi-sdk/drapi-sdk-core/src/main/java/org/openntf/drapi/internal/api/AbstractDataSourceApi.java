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
package org.openntf.drapi.internal.api;

import org.openntf.drapi.DrapiClient;
import org.openntf.drapi.http.ApiPath;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.HttpMethod;

public abstract class AbstractDataSourceApi extends AbstractApi {

    protected static final String QS_DATASOURCE = "dataSource";

    private final String dataSource;

    protected AbstractDataSourceApi(DrapiClient client, String dataSource) {
        super(client);
        this.dataSource = dataSource;
    }

    protected String dataSource() {
        return dataSource;
    }

    // Override the newRequest method to include the dataSource query parameter
    @Override
    protected DrapiRequest newRequest(HttpMethod method, ApiPath apiPath) {
        return super.newRequest(method, apiPath)
                    .queryParam(QS_DATASOURCE, dataSource());
    }
}
