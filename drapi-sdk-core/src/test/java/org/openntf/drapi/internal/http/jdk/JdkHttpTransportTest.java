package org.openntf.drapi.internal.http.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openntf.drapi.internal.http.HttpHeaderNames.USER_AGENT;

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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.http.HttpMethod;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.http.RequestBody;
import org.openntf.drapi.internal.DrapiConfigBuilder;

class JdkHttpTransportTest {

    private HttpServer server;
    private ExecutorService serverPool;
    private DrapiConfig config;

    // AtomicReference to hold the mirrored request for assertions in tests
    private final AtomicReference<DrapiRequest> mirrorRequest = new AtomicReference<>();

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

    private void buildConfig(Consumer<DrapiConfigBuilder> configCustomizer) {
        var builder = DrapiConfig.builder()
                                 .baseUrl(URI.create("http://127.0.0.1:" + server.getAddress().getPort()).toString())
                                 .token("dummy-token");

        if (configCustomizer != null) {
            configCustomizer.accept(builder);
        }

        this.config = builder.build();
    }

    // This method is a placeholder to set the next response for the mirror server.
    // Should be called once per test to set the expected response for the next request.
    void respondWith(int statusCode, String body) {
        respondWith(statusCode, body, null);
    }

    void respondWith(int statusCode, String body, Map<String, List<String>> headers) {
        respondWith(httpExchange -> {
            try {
                mirrorRequest.set(createMirrorRequest(httpExchange));

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

    void respondWith(Consumer<HttpExchange> customHandler) {
        server.createContext("/", exchange -> {
            mirrorRequest.set(createMirrorRequest(exchange));

            if (customHandler != null) {
                customHandler.accept(exchange);
            }

            exchange.close();
        });
    }

    HttpTransport createTransport() {
        return new JdkHttpTransport(config, null);
    }

    @Test
    @DisplayName("Basic GET request should be mirrored correctly and return expected response")
    void getRequestSendsPathAndMethod() {
        buildConfig(null);
        respondWith(200, "Hello World", Map.of("X-Test-Header", List.of("some-value", "another-value")));

        try (var response = createTransport().submit(DrapiRequest.get("/test").queryParam("p1", "v1").queryParam("p2", "v2 v3"))) {
            DrapiRequest mirroredRequest = mirrorRequest.get();

            assertEquals("/test", mirroredRequest.path(), "The mirrored request path should match the original request path");
            assertTrue(mirroredRequest.containsQueryParam("p1", "v1"), "The mirrored request should contain query parameter p1=v1");
            assertTrue(mirroredRequest.containsQueryParam("p2", "v2 v3"), "The mirrored request should contain query parameter p2=v2 v3");
            assertEquals(HttpMethod.GET, mirroredRequest.httpMethod(), "The mirrored request HTTP method should match the original request HTTP method");
            assertTrue(mirroredRequest.containsHeader(USER_AGENT, config.userAgent()), "The mirrored request should have the correct User-Agent header");

            assertEquals(200, response.statusCode(), "The response status code should match the expected status code");
            assertEquals("Hello World", response.bodyAsString(), "The response body should match the expected body");
            assertTrue(response.containsHeader("X-Test-Header", "some-value"), "The mirrored request should have the correct X-Test-Header header");
            assertTrue(response.containsHeader("X-Test-Header", "another-value"), "The mirrored request should have the correct X-Test-Header header");
        }
    }

    @Test
    @DisplayName("Test that server returns 404 and we can detect that normally")
    void test404Response() {
        buildConfig(null);
        respondWith(404, "Not Found");

        try (var response = createTransport().submit(DrapiRequest.get("/nonexistent"))) {
            assertEquals(404, response.statusCode(), "The response status code should be 404 for a nonexistent resource");
            assertEquals("Not Found", response.bodyAsString(), "The response body should indicate that the resource was not found");
            assertFalse(response.isSuccess(), "The response should not be considered successful for a 404 status code");
        }
    }

    @Test
    @DisplayName("Test that server returns 401 and we can detect that normally")
    void test401Response() {
        buildConfig(null);
        respondWith(401, "Unauthorized");

        try (var response = createTransport().submit(DrapiRequest.get("/nonexistent"))) {
            assertEquals(401, response.statusCode(), "The response status code should be 401 for a nonexistent resource");
            assertFalse(response.isSuccess(), "The response should not be considered successful for a 401 status code");
            assertTrue(response.isAuthenticationFailure(), "The response should be considered an authentication failure for a 401 status code");
        }
    }

    @Test
    @DisplayName("Simulate unresponsive server and ensure that the transport handles it gracefully")
    void testUnresponsiveServer() {
        buildConfig(builder -> {
            builder.baseUrl(URI.create("http://127.0.0.1:" + unusedPort()).toString()); // Use an unused port to simulate unresponsiveness
        });
        respondWith(200, "Successful Response"); // This response will never be reached due to the unused port

        try (var response = createTransport().submit(DrapiRequest.get("/test"))) {
            throw new AssertionError("Expected an exception due to unresponsive server, but got a response: " + response.statusCode());
        } catch (RuntimeException e) {
            // We expect a RuntimeException due to the unresponsive server. The exact exception type may vary based on the underlying HTTP client implementation.
            assertInstanceOf(RuntimeException.class, e,
                             "Expected a RuntimeException due to unresponsive server, but got: " + e.getCause());

            // TODO Revisit this after we have a more robust exception handling mechanism in place. For now, we just check that an exception was thrown.
        }
    }

    DrapiRequest createMirrorRequest(HttpExchange exchange) throws IOException {
        var request = DrapiRequest.create(HttpMethod.of(exchange.getRequestMethod()), exchange.getRequestURI().getPath())
                                  .headers(exchange.getRequestHeaders())
                                  .body(RequestBody.ofBytes(exchange.getRequestHeaders().getFirst("Content-Type"),
                                                            exchange.getRequestBody().readAllBytes()));

        parseQueryParams(request, exchange.getRequestURI());

        return request;
    }

    // Untested testing function...
    void parseQueryParams(DrapiRequest request, URI uri) {
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

    private static int unusedPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Could not reserve a port", e);
        }
    }


}
