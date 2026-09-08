package org.openntf.drapi.internal.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.openntf.drapi.api.ListsApi;
import org.openntf.drapi.api.options.ListsGetOptions;
import org.openntf.drapi.http.ApiPath;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.internal.DrapiContext;
import org.openntf.drapi.internal.log.Log;
import org.openntf.drapi.internal.meta.ResponseParser;
import org.openntf.drapi.meta.ListEntry;

public class ListsApiImpl extends AbstractDataSourceApi implements ListsApi {

    private static final Log LOG = Log.getLogger(ListsApiImpl.class);
    public static final ApiPath LIST_API_PATH = ApiPath.root("/lists");

    public ListsApiImpl(DrapiContext context, String dataSource) {
        super(context, dataSource);
    }

    @Override
    public CompletableFuture<Stream<ListEntry>> get(String viewName, ListsGetOptions options) {
        DrapiRequest request = DrapiRequest.get(LIST_API_PATH.append(viewName))
                                           .queryParam(QS_DATASOURCE, dataSource());

        if (options != null) {
            options.toParameterList().forEach(request::queryParam);
        }

        LOG.trace("Submitting request for {}", request.path());

        return submitRequest(request, ResponseParser::toListEntryStream);

    }


}
