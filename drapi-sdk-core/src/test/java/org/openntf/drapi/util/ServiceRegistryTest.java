package org.openntf.drapi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ServiceConfigurationError;
import org.junit.jupiter.api.Test;

class ServiceRegistryTest {

    @Test
    void failWhenNoServiceFound() {
        Throwable t = assertThrows(ServiceConfigurationError.class, () -> {
            ServiceRegistry.findService(NonExistentService.class);
        });

        assertEquals("No implementation found for: " + NonExistentService.class.getName(), t.getMessage());
    }

    @Test
    void findServiceSuccessfully() {
        var testService = ServiceRegistry.findService(TestService.class);

        assertNotNull(testService);
        assertEquals("TestServiceImpl", testService.getMessage());
    }

    @Test
    void assertServiceCaching() {
        var firstCall = ServiceRegistry.findService(TestService.class);
        var secondCall = ServiceRegistry.findService(TestService.class);

        assertNotNull(firstCall);
        assertNotNull(secondCall);
        assertSame(firstCall, secondCall); // Should be the same instance due to caching
    }

    @Test
    void duplicateServiceShouldNotBeAllowed() {
        Throwable t = assertThrows(ServiceConfigurationError.class, () -> {
            ServiceRegistry.findService(AnotherTestService.class);
        });

        assertEquals("Multiple implementations found for: " + AnotherTestService.class.getName(), t.getMessage());
    }

    public interface NonExistentService {

        String getMessage();
    }


    public interface TestService {

        String getMessage();
    }

    public static class TestServiceImpl implements TestService {

        @Override
        public String getMessage() {
            return "TestServiceImpl";
        }
    }

    public interface AnotherTestService {

        String someThingElse();
    }

    public static class AnotherTestServiceImpl implements AnotherTestService {

        @Override
        public String someThingElse() {
            return "AnotherTestServiceImpl";
        }
    }

    public static class AnotherTestServiceSecondImpl implements AnotherTestService {

        @Override
        public String someThingElse() {
            return "AnotherTestServiceSecondImpl";
        }
    }
}
