package org.openntf.drapi.api.options;

import java.util.Locale;

public enum KeyType {

    NUMBER,
    TEXT,
    TIME;

    public String value() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    public static KeyType fromString(String value) {
        for (KeyType keyType : KeyType.values()) {
            if (keyType.name().equalsIgnoreCase(value)) {
                return keyType;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }

}
