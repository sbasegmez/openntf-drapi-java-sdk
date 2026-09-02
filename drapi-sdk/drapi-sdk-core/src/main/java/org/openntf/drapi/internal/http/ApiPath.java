package org.openntf.drapi.internal.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openntf.drapi.util.TypeUtils;

public class ApiPath {

    private final List<String> segments;

    private ApiPath() {
        this.segments = new ArrayList<>();
    }

    public static ApiPath root() {
        return of("/api/v1");
    }

    public static ApiPath root(String additionalSegments) {
        return root().append(additionalSegments);
    }

    public static ApiPath empty() {
        return new ApiPath();
    }

    public static ApiPath of(String segments) {
        return new ApiPath().append(segments);
    }

    public ApiPath append(String segment) {
        if (TypeUtils.isNotBlank(segment)) {
            Arrays.stream(segment.split("/")).forEach(part -> {
                if (TypeUtils.isNotBlank(part)) {
                    this.segments.add(normalizeAndEncodePath(part));
                }
            });
        }
        return this;
    }

    @Override
    public String toString() {
        String result = String.join("/", segments);

        if(!result.startsWith("/")) {
            result = "/" + result;
        }
        if(result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }


    private static String normalizeAndEncodePath(String path) {
        String trimmed = path.trim();

        int start = trimmed.startsWith("/") ? 1 : 0;
        int end = trimmed.endsWith("/") ? trimmed.length() - 1 : trimmed.length();

        return UriBuilder.urlEncode(trimmed.substring(start, end).trim());
    }

}
