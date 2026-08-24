package org.openntf.drapi.util;

import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public class ServiceRegistry {

    /**
     * Find a service implementation for the given service class. If no implementation is found, throw a ServiceConfigurationError. If
     * multiple implementations are found, throw a ServiceConfigurationError. This method is useful for cases where you want to ensure
     * that there is exactly one implementation of a service interface. This method does not cache the result. So it's recommended to
     * cache the result if you expect to call this method multiple times for the same service
     *
     * @param serviceClass The service interface class for which an implementation is to be found.
     * @param <T> The type of the service interface.
     * @return An instance of the service implementation.
     */
    public static <T> T findService(Class<T> serviceClass) {
        // Use the ServiceLoader to find the service implementation. We use default TCCL (Thread Context Class Loader).
        List<T> list = ServiceLoader.load(serviceClass)
                                    .stream()
                                    .map(ServiceLoader.Provider::get)
                                    .toList();

        // TODO: Consider testing for weird environments like OSGi/GraalVM to ensure this works correctly in such contexts.

        if (list.isEmpty()) {
            throw new java.util.ServiceConfigurationError("No implementation found for: " + serviceClass.getName());
        }

        // We throw an error if more than one implementation is found.
        if (list.size() > 1) {
            throw new java.util.ServiceConfigurationError("Multiple implementations found for: " + serviceClass.getName());
        }

        return list.get(0);
    }

    /**
     * Find a service implementation for the given service class. If no implementation is found, return the default instance provided by
     * the supplier. If multiple implementations are found, return the first one found, which is not deterministic. This method is
     * useful for cases where you want to provide a default implementation but also allow for SPI-based overrides. This method does not
     * cache the result. So it's recommended to cache the result if you expect to call this method multiple times for the same service
     * class.
     *
     * @param serviceClass The service interface class for which an implementation is to be found.
     * @param supplier     A supplier that provides a default instance of the service implementation if none is found.
     * @param <T>          The type of the service interface.
     * @return An instance of the service implementation.
     */
    public static <T> T findServiceOrDefault(Class<T> serviceClass, Supplier<T> supplier) {
        return ServiceLoader.load(serviceClass)
                            .stream()
                            .map(ServiceLoader.Provider::get)
                            .findFirst()
                            .orElseGet(supplier);
    }


}
