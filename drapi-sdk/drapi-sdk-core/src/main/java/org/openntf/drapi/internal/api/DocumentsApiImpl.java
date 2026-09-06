package org.openntf.drapi.internal.api;

import java.util.concurrent.CompletableFuture;
import org.openntf.drapi.DrapiContext;
import org.openntf.drapi.api.DocumentsApi;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.internal.http.ApiPath;
import org.openntf.drapi.internal.log.Log;
import org.openntf.drapi.internal.meta.ResponseParser;
import org.openntf.drapi.meta.Document;
import org.openntf.drapi.meta.RichTextAs;

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

    public static final class GetOptions {

        private String mode = null;
        private Boolean meta = null;
        private RichTextAs richTextAs = null;
        private Boolean markRead = null;
        private Boolean markUnread = null;

        public GetOptions withMode(String mode) {
            this.mode = mode;
            return this;
        }

        public GetOptions withMeta(Boolean meta) {
            this.meta = meta;
            return this;
        }

        public GetOptions withRichTextAs(RichTextAs richTextAs) {
            this.richTextAs = richTextAs;
            return this;
        }

        public GetOptions withMarkRead(Boolean markRead) {
            this.markRead = markRead;
            return this;
        }

        public GetOptions withMarkUnread(Boolean markUnread) {
            this.markUnread = markUnread;
            return this;
        }

        public String mode() {
            return mode;
        }

        public Boolean meta() {
            return meta;
        }

        public RichTextAs richTextAs() {
            return richTextAs;
        }

        public Boolean markRead() {
            return markRead;
        }

        public Boolean markUnread() {
            return markUnread;
        }

        public void applyToRequest(DrapiRequest request) {
            if (mode != null) {
                request.queryParam("mode", mode);
            }
            if (meta != null) {
                request.queryParam("meta", meta.toString());
            }
            if (richTextAs != null) {
                request.queryParam("richTextAs", richTextAs.value());
            }
            if (markRead != null) {
                request.queryParam("markRead", markRead.toString());
            }
            if (markUnread != null) {
                request.queryParam("markUnread", markUnread.toString());
            }
        }

    }

}
