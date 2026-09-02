package org.openntf.drapi.json;

/**
 * Test support class for JsonBinding.
 * <p>
 * Test classes using JsonBinding can set a mock or custom implementation of JsonBinding for testing purposes. Set the json binding
 * instance on @BeforeEach and reset it on @AfterEach to ensure that the test does not affect other tests.
 */
public class JsonBindingTestSupport {

    public static void set(JsonBinding jsonBinding) {
        JsonBinding.JsonBindingHolder.override(jsonBinding);
    }

    public static void reset() {
        JsonBinding.JsonBindingHolder.reset();
    }

}
