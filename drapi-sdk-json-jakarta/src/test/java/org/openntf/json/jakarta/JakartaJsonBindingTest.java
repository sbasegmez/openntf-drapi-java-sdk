package org.openntf.json.jakarta;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openntf.drapi.json.AbstractJsonBindingTest;
import org.openntf.drapi.json.JsonBinding;

class JakartaJsonBindingTest extends AbstractJsonBindingTest {

    private final static JakartaJsonBinding JSON_BINDING = new JakartaJsonBinding();

    @Override
    protected JsonBinding jsonBinding() {
        return JSON_BINDING;
    }

    @Test
    @DisplayName("Test that the JakartaJsonBinding is detected as the default JSON binding")
    void testJsonBindingIsDetected() {
        // Ensure that the JakartaJsonBinding is detected as the default JSON binding
        JsonBinding binding = JsonBinding.get();

        assertNotNull(binding, "Default JSON binding should not be null");
        assertSame(JakartaJsonBinding.class, binding.getClass(), "Default JSON binding should be JakartaJsonBinding");
    }

}
