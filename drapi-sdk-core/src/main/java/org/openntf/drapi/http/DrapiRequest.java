package org.openntf.drapi.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class DrapiRequest {

    // The HTTP method for the request (e.g., GET, POST, PUT, DELETE).
    private final HttpMethod httpMethod;

    // The path of the request, which is the endpoint being accessed (e.g., "/api/resource").
    private final String path;

    // HTTP allows "a=1&b=2&a=3" style query parameters, so we need to support multiple values for the same key.
    private final List<QueryParameter> queryParams;

    // Headers are stored in a case-insensitive manner, as HTTP headers are case-insensitive.
    private final Map<String, List<String>> headers;

    // The body of the request, which can be either a byte array, a streaming input or an empty one.
    private RequestBody body;

    private DrapiRequest(HttpMethod method, String path) {
        this.httpMethod = Objects.requireNonNull(method, "HTTP method cannot be null");
        this.path = Objects.requireNonNull(path, "Path cannot be null");
        this.queryParams = new ArrayList<>();
        this.headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        // Initial value is empty
        this.body = RequestBody.ofEmpty();
    }

    public static DrapiRequest create(HttpMethod method, String path) {
        return new DrapiRequest(method, path);
    }

    public static DrapiRequest get(String path) {
        return new DrapiRequest(HttpMethod.GET, path);
    }

    public static DrapiRequest patch(String path) {
        return new DrapiRequest(HttpMethod.PATCH, path);
    }

    public static DrapiRequest post(String path) {
        return new DrapiRequest(HttpMethod.POST, path);
    }

    public static DrapiRequest put(String path) {
        return new DrapiRequest(HttpMethod.PUT, path);
    }

    public static DrapiRequest delete(String path) {
        return new DrapiRequest(HttpMethod.DELETE, path);
    }

    public RequestBody body() {
        return body;
    }

    public HttpMethod httpMethod() {
        return httpMethod;
    }

    public String path() {
        return path;
    }

    public List<QueryParameter> queryParams() {
        return List.copyOf(queryParams);
    }

    public Map<String, List<String>> headers() {
        Map<String, List<String>> copy = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        return copy;
    }

    public DrapiRequest queryParam(String key, String value) {
        if (value != null) {
            this.queryParams.add(new QueryParameter(key, value));
        }
        return this;
    }

    public DrapiRequest queryParams(Map<String, List<String>> queryParams) {
        queryParams.forEach((key, values) -> {
            for (String value : values) {
                this.queryParams.add(new QueryParameter(key, value));
            }
        });
        return this;
    }

    public DrapiRequest header(String key, String value) {
        if (value != null) {
            this.headers.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return this;
    }

    public DrapiRequest header(String key, List<String> values) {
        if (values != null) {
            this.headers.computeIfAbsent(key, k -> new ArrayList<>()).addAll(values);
        }
        return this;
    }

    public DrapiRequest headers(Map<String, List<String>> headers) {
        headers.forEach(this::header);
        return this;
    }

    public DrapiRequest body(RequestBody body) {
        this.body = Objects.requireNonNull(body, "Request body cannot be null");
        return this;
    }

    public record QueryParameter(String key, String value) {

    }

}
