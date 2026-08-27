package org.openntf.drapi.http;

public enum HttpMethod {
    DELETE,
    GET,
    PATCH,
    POST,
    PUT;

    public static HttpMethod of(String method) {
        for (HttpMethod httpMethod : values()) {
            if (httpMethod.name().equalsIgnoreCase(method)) {
                return httpMethod;
            }
        }
        throw new IllegalArgumentException("Unknown HTTP method: " + method);
    }

}
