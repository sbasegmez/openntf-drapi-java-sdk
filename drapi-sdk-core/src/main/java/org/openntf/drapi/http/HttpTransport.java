package org.openntf.drapi.http;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.internal.http.jdk.JdkHttpTransportProvider;
import org.openntf.drapi.util.ServiceRegistry;

public interface HttpTransport {

    /**
     * Submits a DrapiRequest asynchronously. Implementations of this method should handle the request submission and return a
     * CompletableFuture that will be completed with the DrapiResponse when the request is processed.
     *
     * @param request the DrapiRequest to submit
     * @return a CompletableFuture that will be completed with the DrapiResponse
     */
    CompletableFuture<DrapiResponse> submitAsync(DrapiRequest request);

    /**
     * Submits a DrapiRequest synchronously. This method internally calls submitAsync and waits for the result. If an exception occurs
     * during the submission, it wraps the cause in a RuntimeException and throws it.
     *
     * @param request the DrapiRequest to submit
     * @return the DrapiResponse
     */
    default DrapiResponse submit(DrapiRequest request) {
        try {
            return submitAsync(request).join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();

            // TODO : Handle specific exceptions like IOException, InterruptedException, etc., if needed.

            throw new RuntimeException("Failed to submit request", cause);
        }
    }

    /**
     * Checks if there is an SPI-based implementation of HttpTransport available, and returns it if found. Otherwise, it returns the
     * default implementation (JdkHttpTransport).
     *
     * @return the default HttpTransport implementation
     */
    static HttpTransport defaultTransport(DrapiConfig config, Executor executor) {
        var provider = ServiceRegistry.findServiceOrDefault(HttpTransportProvider.class, JdkHttpTransportProvider::new);

        return provider.create(config, executor);
    }

}
