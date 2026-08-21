package org.openntf.drapi.util;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {

    // Service cache by service class to avoid repeated lookups
    private static final Map<Class<?>, Object> SINGLETON_SERVICE_CACHE = new ConcurrentHashMap<>();

    public static <T> T findService(Class<T> serviceClass) {
        return serviceClass.cast(SINGLETON_SERVICE_CACHE.computeIfAbsent(serviceClass, ServiceRegistry::loadService));
    }

    private static <T> T loadService(Class<T> serviceClass) {
        // Use the ServiceLoader to find the service implementation. We use default TCCL (Thread Context Class Loader).
        // TODO: Consider testing for weird environments like OSGi/GraalVM to ensure this works correctly in such contexts.
        List<T> list = ServiceLoader.load(serviceClass)
                                    .stream()
                                    .map(ServiceLoader.Provider::get)
                                    .toList();

        if(list.isEmpty()) {
            throw new java.util.ServiceConfigurationError("No implementation found for: " + serviceClass.getName());
        }

        if(list.size() > 1) {
            throw new java.util.ServiceConfigurationError("Multiple implementations found for: " + serviceClass.getName());
        }

        return list.get(0);
    }

}
