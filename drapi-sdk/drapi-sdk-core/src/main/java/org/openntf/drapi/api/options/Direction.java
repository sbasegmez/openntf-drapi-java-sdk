package org.openntf.drapi.api.options;

// Enum representing the direction of sorting, either ascending or descending.
public enum Direction {
    ASCENDING("asc"),
    DESCENDING("desc");

    private final String value;

    Direction(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Direction of(String value) {
        for (Direction direction : Direction.values()) {
            if (direction.value.equalsIgnoreCase(value)) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Unknown direction value: " + value);
    }
}
