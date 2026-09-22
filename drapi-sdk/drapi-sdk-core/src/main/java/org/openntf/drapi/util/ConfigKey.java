package org.openntf.drapi.util;

import java.util.Objects;
import org.openntf.drapi.DrapiConfig;

/**
 * A configuration key that can be used to retrieve values from a {@link DrapiConfig} instance.
 *
 * @param key the key of the configuration value
 * @param type the type of the configuration value
 * @param defaultValue the default value of the configuration value
 * @param <T> the type of the configuration value
 */
public record ConfigKey<T>(String key, Class<T> type, T defaultValue) {

    public ConfigKey {
        Objects.requireNonNull(key);
        Objects.requireNonNull(type);
    }

    public static <T> ConfigKey<T> of(String key, Class<T> type) {
        return new ConfigKey<>(key, type, null);
    }

    public static <T> ConfigKey<T> of(String key, Class<T> type, T defaultValue) {
        return new ConfigKey<>(key, type, defaultValue);
    }
}
