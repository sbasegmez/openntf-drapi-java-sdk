package org.openntf.drapi.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public sealed interface RequestBody permits RequestBody.Bytes, RequestBody.Streaming {

    InputStream createStream() throws IOException;

    /**
     * Create a RequestBody from a byte array.
     * <p>
     * For long requests, consider using the Streaming variant to avoid loading the entire request body into memory.
     *
     * @param data the byte array containing the request body data
     */
    record Bytes(byte[] data) implements RequestBody {

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
    record Streaming(BodySupplier bodySupplier) implements RequestBody {

        @Override
        public InputStream createStream() throws IOException {
            return bodySupplier.getInputStream();
        }
    }

    @FunctionalInterface
    interface BodySupplier {
        InputStream getInputStream() throws IOException;
    }

    static RequestBody ofEmpty() {
        return new Bytes(new byte[0]);
    }

    static RequestBody ofBytes(byte[] data) {
        return new Bytes(data);
    }

    static RequestBody ofStreaming(BodySupplier bodySupplier) {
        return new Streaming(bodySupplier);
    }

    static RequestBody ofString(String data) {
        return new Bytes(data.getBytes(StandardCharsets.UTF_8));
    }

    static RequestBody ofString(String data, Charset charset) {
        return new Bytes(data.getBytes(charset));
    }

}
