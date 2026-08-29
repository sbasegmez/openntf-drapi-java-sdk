package org.openntf.drapi.internal.http;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.openntf.drapi.util.Parameter;
import org.openntf.drapi.util.TypeUtils;

public class UriBuilder {

    private final StringBuilder stringBuilder;
    private final StringBuilder queryString;

    private UriBuilder(String baseUrl) {
        this.stringBuilder = new StringBuilder(validate(baseUrl));
        this.queryString = new StringBuilder();
    }

    public static UriBuilder startWith(URI base) {
        return new UriBuilder(base.toString());
    }

    public static UriBuilder startWith(String base) {
        return new UriBuilder(base);
    }

    public UriBuilder appendPath(String path) {
        if(TypeUtils.isNotBlank(path)) {
            Arrays.stream(path.split("/")).forEach(segment -> {
                if (TypeUtils.isNotBlank(segment)) {
                    this.stringBuilder.append("/").append(normalizeAndEncodePath(segment));
                }
            });
        }
        return this;
    }

    public UriBuilder appendQueryParams(List<Parameter> parameters) {
        parameters.forEach(this::appendQueryParam);
        return this;
    }

    public UriBuilder appendQueryParam(Parameter parameter) {
        return appendQueryParam(parameter.key(), parameter.value());
    }

    public UriBuilder appendQueryParam(String key, String value) {
        // Normally, it's not illegal to have a query parameter with an empty value, but for DRAPI, there is not such a case.
        if (TypeUtils.isNotBlank(key) && TypeUtils.isNotBlank(value)) {
            if (!queryString.isEmpty()) {
                queryString.append("&");
            }
            queryString.append(urlEncode(key)).append("=").append(urlEncode(value));
        }
        return this;
    }

    public URI build() {
        return URI.create(this.stringBuilder.toString() + (queryString.isEmpty() ? "" : "?" + queryString.toString()));
    }

    private static String validate(String baseUrl) {
        if (TypeUtils.isBlank(baseUrl)) {
            throw new IllegalArgumentException("Base URL cannot be null or blank");
        }

        URI uri;
        try {
            uri = new URI(normalizeUrl(baseUrl));
        } catch (Exception e) {
            throw new IllegalArgumentException("Base URL must be a valid URI: " + e.getMessage());
        }

        if (uri.getScheme() == null || uri.getHost() == null) {
            throw new IllegalArgumentException("Base URL must be a valid URI with scheme and host");
        }

        if (!TypeUtils.startsWithIgnoreCase(uri.getScheme(), "http")) {
            throw new IllegalArgumentException("Base URL must start with http or https");
        }

        if (TypeUtils.isNotBlank(uri.getPath())) {
            throw new IllegalArgumentException("Base URL must not contain a path");
        }

        if (TypeUtils.isNotBlank(uri.getUserInfo())) {
            throw new IllegalArgumentException("Base URL must not contain user info");
        }

        if (TypeUtils.isNotBlank(uri.getQuery()) || TypeUtils.isNotBlank(uri.getFragment())) {
            throw new IllegalArgumentException("Base URL must not contain a query string or fragment");
        }

        return uri.toString();
    }

    private static String normalizeUrl(String baseUrl) {
        String url = baseUrl.trim();

        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1).trim();
        }

        return url;
    }

    private static String normalizeAndEncodePath(String path) {
        String trimmed = path.trim();

        int start = trimmed.startsWith("/") ? 1 : 0;
        int end = trimmed.endsWith("/") ? trimmed.length() - 1 : trimmed.length();

        return urlEncode(trimmed.substring(start, end).trim());
    }

    private static String urlEncode(String value) {
        // We'll go with form-encoding for now, since DRAPI doesn't seem to have much use of this.
        // Most importantly, this will encode spaces as '+' instead of '%20'. It should be fine for DRAPI.
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
