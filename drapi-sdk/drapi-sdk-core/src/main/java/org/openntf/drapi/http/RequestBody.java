package org.openntf.drapi.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public sealed interface RequestBody permits RequestBody.Bytes, RequestBody.Streaming {

    String contentType();
    InputStream createStream();

    /**
     * Create a RequestBody from a byte array.
     * <p>
     * For long requests, consider using the Streaming variant to avoid loading the entire request body into memory.
     *
     * @param data the byte array containing the request body data
     */
    record Bytes(String contentType, byte[] data) implements RequestBody {

        @Override
        public InputStream createStream() {
            return new ByteArrayInputStream(data);
        }
    }

    /**
     * Create a Streaming RequestBody.
     * <p>
     * The user of this variant must conform repeatability. In case SDK fails to submit the request for some reason, it will retry the
     * request. If the body is not repeatable, the request will fail.
     * <p>
     * Common pattern is to use a lambda that creates a new InputStream each time it is called. For example, if you want to stream a
     * file, you can use:
     * <pre>
     *     RequestBody.Streaming streamingBody = new RequestBody.Streaming(() -> new FileInputStream(file));
     * </pre>
     * <p>
     * This is a good example of how to create a repeatable streaming request body. Every time the SDK needs to retry the request, it
     * will call the lambda and get a new fresh InputStream.
     *
     * @param bodySupplier the supplier that provides a new InputStream each time it is called
     */
    record Streaming(String contentType, BodySupplier bodySupplier) implements RequestBody {

        @Override
        public InputStream createStream() {
            try {
                return bodySupplier.getInputStream();
            } catch (IOException e) {
                // Wrap the IOException in a UncheckedIOException to avoid changing the method signature.
                // This is a common pattern when dealing with functional interfaces that don't allow checked exceptions.
                throw new UncheckedIOException(e);
            }
        }
    }

    @FunctionalInterface
    interface BodySupplier {
        InputStream getInputStream() throws IOException;
    }

    static RequestBody ofEmpty() {
        // Empty request body is represented as a byte array with zero length. No content type is specified for empty bodies, but it can be set if needed.
        return new Bytes(null, new byte[0]);
    }

    static RequestBody ofBytes(String contentType, byte[] data) {
        return new Bytes(contentType, data);
    }

    static RequestBody ofStreaming(String contentType, BodySupplier bodySupplier) {
        return new Streaming(contentType, bodySupplier);
    }

    static RequestBody ofString(String contentType, String data) {
        return new Bytes(contentType, data.getBytes(StandardCharsets.UTF_8));
    }

    static RequestBody ofString(String contentType, String data, Charset charset) {
        return new Bytes(contentType, data.getBytes(charset));
    }

}
