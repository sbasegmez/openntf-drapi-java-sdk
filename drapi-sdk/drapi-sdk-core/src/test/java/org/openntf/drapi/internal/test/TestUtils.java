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
package org.openntf.drapi.internal.test;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.HttpMethod;
import org.openntf.drapi.http.RequestBody;

public class TestUtils {

    public static void runMultipleThreadsAtTheSameTime(int numberOfThreads, Runnable task) {

        CyclicBarrier barrier = new CyclicBarrier(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    barrier.await(); // Wait for all threads to be ready
                    task.run(); // Execute the task
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                } catch (BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        executorService.shutdown();

        futures.forEach(future -> {
            try {
                future.get(); // Wait for each task to complete
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    protected static DrapiRequest createMirrorRequest(HttpExchange exchange) throws IOException {
        var request = DrapiRequest.create(HttpMethod.of(exchange.getRequestMethod()), exchange.getRequestURI().getPath())
                                  .headers(exchange.getRequestHeaders())
                                  .body(RequestBody.ofBytes(exchange.getRequestHeaders().getFirst("Content-Type"),
                                                            exchange.getRequestBody().readAllBytes()));

        parseQueryParams(request, exchange.getRequestURI());

        return request;
    }

    // Untested testing function...
    protected static void parseQueryParams(DrapiRequest request, URI uri) {
        String query = uri.getRawQuery();
        if (query != null && !query.isEmpty()) {
            Arrays.stream(query.split("&"))
                  .map(pair -> pair.split("=", 2))
                  .forEach(keyValue -> {
                      String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                      String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8) : "";
                      request.queryParam(key, value);
                  });
        }
    }

}
