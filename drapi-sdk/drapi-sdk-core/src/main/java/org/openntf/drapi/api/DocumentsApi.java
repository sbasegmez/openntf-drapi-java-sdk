package org.openntf.drapi.api;

import java.util.concurrent.CompletableFuture;
import org.openntf.drapi.internal.api.DocumentsApiImpl.GetOptions;
import org.openntf.drapi.meta.Document;

public interface DocumentsApi {

    CompletableFuture<Document> get(String documentId);

    CompletableFuture<Document> get(String documentId, GetOptions options);

}
