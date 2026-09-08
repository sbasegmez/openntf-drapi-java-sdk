package org.openntf.drapi.api.options;

public enum QueryScope {

    ALL,
    CATEGORIES,
    DOCUMENTS;

    public String value() {
        return name().toLowerCase();
    }

    public static QueryScope of(String value) {
        for (QueryScope scope : QueryScope.values()) {
            if (scope.name().equalsIgnoreCase(value)) {
                return scope;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }

}
