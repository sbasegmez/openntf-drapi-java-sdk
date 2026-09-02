package org.openntf.drapi.internal.test;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
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
import org.openntf.drapi.http.HttpMethod;
import org.openntf.drapi.http.RequestBody;
import org.openntf.drapi.internal.DrapiConfigBuilder;

public class AbstractHttpMockTest {

    protected HttpServer server;
    protected ExecutorService serverPool;
    private DrapiConfig config;
    private HttpContext currentContext;

    // AtomicReference to hold the mirrored request for assertions in tests
    protected final AtomicReference<DrapiRequest> mirrorRequest = new AtomicReference<>();

    // AtomicInteger to keep track of the number of requests received by the mirror server
    protected final AtomicInteger requestCount = new AtomicInteger(0);

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

        this.mirrorRequest.set(null); // Reset the mirror request before each test
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

    protected DrapiConfig config() {
        if (config == null) {
            throw new IllegalStateException("Config has not been built yet. Call buildConfig() before using config().");
        }
        return config;
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

    // This method is a placeholder to set the next response for the mirror server.
    // Can be called multiple times per test to set the expected response for the next request.
    protected void respondWith(int statusCode, String body) {
        respondWith(statusCode, body, null);
    }

    protected void respondWith(int statusCode, String body, Map<String, List<String>> headers) {
        respondWith(httpExchange -> {
            try {
                mirrorRequest.set(TestUtils.createMirrorRequest(httpExchange));

                if (headers != null) {
                    httpExchange.getResponseHeaders().putAll(headers);
                }

                httpExchange.sendResponseHeaders(statusCode, body.getBytes(StandardCharsets.UTF_8).length);
                httpExchange.getResponseBody().write(body.getBytes(StandardCharsets.UTF_8));
                httpExchange.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }

    protected void respondWith(Consumer<HttpExchange> customHandler) {
        // Remove any existing context to avoid conflicts
        if (currentContext != null) {
            server.removeContext(currentContext);
        }

        // Add a new context with the custom handler
        currentContext = server.createContext(pathToListen(), exchange -> {
            mirrorRequest.set(TestUtils.createMirrorRequest(exchange));
            requestCount.incrementAndGet();

            if (customHandler != null) {
                customHandler.accept(exchange);
            }

            exchange.close();
        });
    }

    protected String pathToListen() {
        return "/";
    }

    protected static int findUnusedPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Could not reserve a port", e);
        }
    }

}
