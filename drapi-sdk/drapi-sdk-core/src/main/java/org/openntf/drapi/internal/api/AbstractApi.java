package org.openntf.drapi.internal.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.openntf.drapi.DrapiContext;
import org.openntf.drapi.exception.AuthenticationException;
import org.openntf.drapi.exception.DrapiException;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.internal.log.Log;

public abstract class AbstractApi {

    private static final Log LOG = Log.getLogger(AbstractApi.class);

    private final DrapiContext context;

    protected AbstractApi(DrapiContext context) {
        this.context = context;
    }

    protected DrapiContext context() {
        return context;
    }

    protected <T> CompletableFuture<T> submitRequest(DrapiRequest request, Function<DrapiResponse, T> mapper) {
        return context().httpTransport()
                        .submitAsync(request)
                        .thenCompose(response -> peekResponse(request, response))
                        .thenApply(mapper);
    }

    protected CompletableFuture<DrapiResponse> peekResponse(DrapiRequest request, DrapiResponse response) {
        if(response.isSuccess()) {
            LOG.trace("Request successful for {}", request.path());
            return CompletableFuture.completedFuture(response);
        }

        Exception ex = response.isAuthenticationFailure() ?
                new AuthenticationException("Authentication failed", request, response) :
                new DrapiException("Request failed", request, response);

        LOG.debug("Request failed for {}: {}", request.path(), ex.getMessage());

        return CompletableFuture.failedFuture(ex);
    }

}
