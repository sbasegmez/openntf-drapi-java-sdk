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
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.internal.DrapiConfigBuilder;

public class MockableHttpTest {

    protected HttpServer server;
    protected ExecutorService serverPool;
    protected DrapiConfig config;

    // AtomicInteger to keep track of the number of requests received by the mirror server
    protected AtomicInteger requestCount = new AtomicInteger(0);

    // AtomicReference to hold the mirrored request for assertions in tests
    protected final AtomicReference<DrapiRequest> mirrorRequest = new AtomicReference<>();

    @BeforeEach
    void setup() throws IOException {
        // Set up mirror server before all tests
        server = HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
        serverPool = Executors.newCachedThreadPool((runnable) -> {
            Thread thread = new Thread(runnable);
            // Set the thread as a daemon thread so it doesn't block JVM shutdown
            thread.setDaemon(true);
            return thread;
        });
        server.setExecutor(serverPool);
        server.start();
    }

    @AfterEach
    void teardown() {
        // Stop the server after each test
        if (server != null) {
            server.stop(0);
        }
        if (serverPool != null) {
            serverPool.shutdownNow();
        }
    }

    protected DrapiConfig buildConfig(Consumer<DrapiConfigBuilder> configCustomizer) {
        return buildConfig(configCustomizer, true);
    }

    protected DrapiConfig buildConfig(Consumer<DrapiConfigBuilder> configCustomizer, boolean addTokenMethod) {
        var builder = DrapiConfig.builder()
                                 .baseUrl(URI.create("http://127.0.0.1:" + server.getAddress().getPort()).toString());

        if (addTokenMethod) {
            builder.token("dummy-token");
        }

        if (configCustomizer != null) {
            configCustomizer.accept(builder);
        }

        this.config = builder.build();
        return this.config;
    }

    protected void respondWith(Responder responder) {
        server.createContext("/", exchange -> {
            requestCount.incrementAndGet();
            mirrorRequest.set(TestUtils.createMirrorRequest(exchange));

            MockResponse mockResponse = responder.respond(exchange);

            if (mockResponse.headers() != null) {
                mockResponse.headers().forEach((name, value) -> exchange.getResponseHeaders().add(name, value));
            }

            exchange.sendResponseHeaders(mockResponse.statusCode(), mockResponse.body().getBytes().length);
            exchange.getResponseBody().write(mockResponse.body().getBytes());
            exchange.close();
        });
    }

    protected MockResponse response(int statusCode, String body) {
        return new MockResponse(statusCode, body, null);
    }

    protected MockResponse response(int statusCode, String body, Map<String, String> headers) {
        return new MockResponse(statusCode, body, headers);
    }

    public record MockResponse(int statusCode, String body, Map<String, String> headers) {

    }

    public interface Responder {

        MockResponse respond(HttpExchange exchange);
    }

}
