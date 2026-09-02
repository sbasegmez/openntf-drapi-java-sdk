/*
 * Copyright (c) 2026 Serdar Basegmez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.drapi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ServiceConfigurationError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServiceRegistryTest {

    @Test
    @DisplayName("Test no service found scenario")
    void failWhenNoServiceFound() {
        Throwable t = assertThrows(ServiceConfigurationError.class, () -> ServiceRegistry.findService(NonExistentService.class));

        assertEquals("No implementation found for: " + NonExistentService.class.getName(), t.getMessage());
    }

    @Test
    @DisplayName("Test find service successfully scenario")
    void findServiceSuccessfully() {
        var testService = ServiceRegistry.findService(TestService.class);

        assertNotNull(testService);
        assertEquals("TestServiceImpl", testService.getMessage());
    }

    @Test
    @DisplayName("Test duplicate service scenario")
    void duplicateServiceShouldNotBeAllowed() {
        Throwable t = assertThrows(ServiceConfigurationError.class, () -> ServiceRegistry.findService(AnotherTestService.class));

        assertEquals("Multiple implementations found for: " + AnotherTestService.class.getName(), t.getMessage());
    }

    @Test
    @DisplayName("Test find service or default falls back to default scenario")
    void findServiceOrDefaultFallsBackToDefault() {
        NonExistentService defaultService = () -> "DefaultService";
        NonExistentService service = ServiceRegistry.findServiceOrDefault(NonExistentService.class, () -> defaultService);

        assertNotNull(service, "Service should not be null when falling back to default");
        assertSame(defaultService, service, "Service should fall back to default when no implementation is found");
    }

    @Test
    @DisplayName("Test duplicate service might be allowed scenario")
    void duplicateServiceMightBeAllowed() {
        AnotherTestService service = ServiceRegistry.findServiceOrDefault(AnotherTestService.class, () -> null);

        assertNotNull(service, "Service should not fall back to default even if multiple implementations exist");
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
