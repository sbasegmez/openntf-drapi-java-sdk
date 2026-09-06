package org.openntf.drapi.internal.api;

import java.util.concurrent.CompletableFuture;
import org.openntf.drapi.DrapiContext;
import org.openntf.drapi.api.DocumentsApi;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.internal.http.ApiPath;
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
    public CompletableFuture<Document> get(String documentId, GetOptions options) {
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
