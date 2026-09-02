package org.openntf.drapi.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.openntf.drapi.internal.log.Log;
import org.openntf.drapi.util.TypeUtils;

/**
 * Represents an HTTP response from the DRAPI server.
 * <p>
 * The closing response is important to free up resources, especially when dealing with streaming responses. Always ensure to close the
 * response after processing it.
 */
public final class DrapiResponse implements AutoCloseable {

    private static final Log LOG = Log.getLogger(DrapiResponse.class);

    // The HTTP status code of the response, e.g., 200 for OK, 404 for Not Found, etc.
    private final int statusCode;

    // Basedon HttpResponse, headers are represented as a Map where the key is the header name and the value is a List of header values.
    private final Map<String, List<String>> headers;

    // The body of the response is represented as an InputStream to allow for streaming large responses without loading them entirely into memory.
    private final InputStream bodyStream;

    // Cache for the body as a string to avoid multiple reads errors
    private String bodyStringCache = null;

    public DrapiResponse(int statusCode, Map<String, List<String>> givenHeaders, InputStream bodyStream) {
        this.statusCode = statusCode;
        this.bodyStream = bodyStream;

        // Deep copy of headers to ensure immutability and case-insensitivity.
        this.headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        if( givenHeaders != null) {
            givenHeaders.forEach((key, value) -> this.headers.put(key, List.copyOf(value)));
        }
    }

    public int statusCode() {
        return statusCode;
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    public boolean isAuthenticationFailure() {
        return statusCode == 401;
    }

    public InputStream bodyStream() {
        return bodyStream;
    }

    public byte[] bodyAsBytes() {
        if (bodyStream == null) {
            return new byte[0];
        }
        try {
            return bodyStream.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read response body", e);
        }
    }

    public String bodyAsString() {
        if (bodyStringCache == null) {
            bodyStringCache = new String(bodyAsBytes(), StandardCharsets.UTF_8);
        }
        return bodyStringCache;
    }

    public boolean containsHeader(String headerName) {
        if (TypeUtils.isBlank(headerName)) {
            return false;
        }
        return headers.containsKey(headerName);
    }

    public boolean containsHeader(String headerName, String value) {
        if (TypeUtils.isBlank(headerName) || TypeUtils.isBlank(value)) {
            return false;
        }

        List<String> values = headers.get(headerName);
        return values != null && values.contains(value);
    }

    public Optional<List<String>> getHeaderValues(String headerName) {
        if (TypeUtils.isBlank(headerName)) {
            return Optional.empty();
        }
        return Optional.ofNullable(headers.get(headerName)).map(List::copyOf);
    }

    @Override
    public void close() {
        if (bodyStream != null) {
            try {
                bodyStream.close();
            } catch (IOException e) {
                // Log and swallow the exception to avoid throwing during close, which can be problematic in try-with-resources blocks.
                LOG.warn("Failed to close response body stream", e);
            }
        }
    }

}
