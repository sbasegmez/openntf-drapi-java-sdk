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
package org.openntf.drapi.internal.http.jdk;

import static org.openntf.drapi.internal.http.HttpHeaderConstants.USER_AGENT;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.exception.HttpTransportException;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.DrapiResponse;
import org.openntf.drapi.http.HttpTransportBase;
import org.openntf.drapi.internal.http.UriBuilder;
import org.openntf.drapi.util.Parameter;

public class JdkHttpTransport extends HttpTransportBase {

    private final HttpClient httpClient;

    JdkHttpTransport(DrapiConfig config, Executor executor) {
        super(config, executor);

        this.httpClient = createHttpClient();
    }

    private HttpClient createHttpClient() {
        var builder = HttpClient.newBuilder()
                                .connectTimeout(Duration.ofSeconds(config().connectTimeoutSecs()));

        executor().ifPresent(builder::executor);

        return builder.build();
    }

    /**
     * Submits a DrapiRequest asynchronously. Implementations of this method should handle the request submission and return a
     * CompletableFuture that will be completed with the DrapiResponse when the request is processed.
     *
     * @param drapiRequest the DrapiRequest to submit
     * @return a CompletableFuture that will be completed with the DrapiResponse
     */
    @Override
    public CompletableFuture<DrapiResponse> submitAsync(DrapiRequest drapiRequest) {
        try {
            HttpRequest httpRequest = toHttpRequest(drapiRequest);

            // By default, we'll handle InputStream responses.
            return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream())
                             .thenApply(this::toDrapiResponse)
                             .exceptionally(ex -> {
                                 throw new HttpTransportException("Connection failed", ex);
                             });

        } catch (Exception e) {
            CompletableFuture<DrapiResponse> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    // Convert DrapiRequest to HTTP Request
    private HttpRequest toHttpRequest(DrapiRequest drapiRequest) {

        URI uri = UriBuilder.startWith(config().baseUrl())
                            .appendPath(drapiRequest.path())
                            .appendQueryParams(drapiRequest.queryParams())
                            .build();

        BodyPublisher bodyPublisher = BodyPublishers.ofInputStream(() -> drapiRequest.body().createStream());

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                                                 .uri(uri)
                                                 .timeout(Duration.ofSeconds(config().requestTimeoutSecs()))
                                                 .header(USER_AGENT, config().userAgent())
                                                 .method(drapiRequest.httpMethod().name(), bodyPublisher);

        drapiRequest.headers()
                    .entrySet()
                    .stream()
                    .flatMap(entry -> entry.getValue().stream().map(value -> new Parameter(entry.getKey(), value)))
                    .forEach(param -> builder.header(param.key(), param.value()));

        return builder.build();
    }

    private DrapiResponse toDrapiResponse(HttpResponse<InputStream> httpResponse) {
        return new DrapiResponse(httpResponse.statusCode(), httpResponse.headers().map(), httpResponse.body());
    }

}
