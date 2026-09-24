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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.openntf.drapi.DrapiClient;
import org.openntf.drapi.auth.SessionContext;
import org.openntf.drapi.exception.AuthenticationException;
import org.openntf.drapi.exception.DrapiException;
import org.openntf.drapi.http.ApiPath;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.http.HttpMethod;
import org.openntf.drapi.internal.log.Log;

public abstract class AbstractApi {

    private static final Log LOG = Log.getLogger(AbstractApi.class);

    private final DrapiClient client;

    protected AbstractApi(DrapiClient client) {
        this.client = Objects.requireNonNull(client, "DrapiClient must not be null");
    }

    protected DrapiClient client() {
        return client;
    }

    protected SessionContext sessionContext() {
        return client.sessionContext();
    }

    protected <T> CompletableFuture<T> submitRequest(DrapiRequest request, Function<DrapiResponse, T> mapper) {
        return client().parent()
                       .transport()
                       .submitAsync(request)
                       .thenCompose(response -> peekResponse(request, response))
                       .thenApply(mapper);
    }

    protected CompletableFuture<DrapiResponse> peekResponse(DrapiRequest request, DrapiResponse response) {
        if (response.isSuccess()) {
            LOG.trace("Request successful for {}", request.path());
            return CompletableFuture.completedFuture(response);
        }

        Exception ex = response.isAuthenticationFailure() ?
            new AuthenticationException("Authentication failed", request, response) :
            new DrapiException("Request failed", request, response);

        LOG.debug("Request failed for {}: {}", request.path(), ex.getMessage());

        // Close response to free resources
        response.close();

        return CompletableFuture.failedFuture(ex);
    }

    protected DrapiRequest newRequest(HttpMethod method, ApiPath apiPath) {
        return DrapiRequest.create(method, apiPath)
                           .sessionContext(sessionContext());
    }

    public DrapiRequest newRequestGet(ApiPath apiPath) {
        return newRequest(HttpMethod.GET, apiPath);
    }

    public DrapiRequest newRequestPatch(ApiPath apiPath) {
        return newRequest(HttpMethod.PATCH, apiPath);
    }

    public DrapiRequest newRequestPost(ApiPath apiPath) {
        return newRequest(HttpMethod.POST, apiPath);
    }

    public DrapiRequest newRequestPut(ApiPath apiPath) {
        return newRequest(HttpMethod.PUT, apiPath);
    }

    public DrapiRequest newRequestDelete(ApiPath apiPath) {
        return newRequest(HttpMethod.DELETE, apiPath);
    }

}
