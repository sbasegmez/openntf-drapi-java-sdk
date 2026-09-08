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

import java.util.concurrent.CompletableFuture;
import org.openntf.drapi.api.DocumentsApi;
import org.openntf.drapi.api.options.DocumentsGetOptions;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.ApiPath;
import org.openntf.drapi.internal.DrapiContext;
import org.openntf.drapi.internal.log.Log;
import org.openntf.drapi.internal.meta.ResponseParser;
import org.openntf.drapi.meta.Document;

public class DocumentsApiImpl extends AbstractDataSourceApi implements DocumentsApi {

    private static final Log LOG = Log.getLogger(DocumentsApiImpl.class);

    public DocumentsApiImpl(DrapiContext context, String dataSource) {
        super(context, dataSource);
    }

    @Override
    public CompletableFuture<Document> get(String documentId) {
        return get(documentId, null);
    }

    @Override
    public CompletableFuture<Document> get(String documentId, DocumentsGetOptions options) {
        DrapiRequest request = DrapiRequest.get(ApiPath.root("/document")
                                                       .append(documentId))
                                           .queryParam(QS_DATASOURCE, dataSource());

        if (options != null) {
            options.applyToRequest(request);
        }

        LOG.trace("Submitting request for {}", request.path());

        return submitRequest(request, ResponseParser::toDocument);
    }

}
