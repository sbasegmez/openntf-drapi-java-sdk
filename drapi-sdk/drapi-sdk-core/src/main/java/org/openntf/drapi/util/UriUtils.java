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

import java.net.URI;
import org.openntf.drapi.internal.http.UrlEncoder;

public class UriUtils {

    private UriUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Encodes a path segment for use in a URI.
     * <p>
     * This method is delegate to the UrlEncoder.encode method, which handles the encoding of special characters in the path segment.
     *
     * @param value the path segment to encode
     * @return the encoded path segment
     */
    public static String encodePathSegment(String value) {
        return UrlEncoder.encode(value);
    }

    /**
     * Encodes a query parameter for use in a URI.
     * <p>
     * This method is delegate to the UrlEncoder.encode method, which handles the encoding of special characters in the query parameter.
     *
     * @param value the query parameter to encode
     * @return the encoded query parameter
     */
    public static String encodeQueryParam(String value) {
        return UrlEncoder.encode(value);
    }

    /**
     * Validates the base URL to ensure it is a valid URI with scheme and host, and does not contain a path, user info, query string, or fragment.
     *
     * @param baseUrl the base URL to validate
     * @return the normalized base URL if valid
     */
    public static String validateBaseUrl(String baseUrl) {
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

    /**
     * Normalizes the base URL by trimming whitespace and removing any trailing slashes.
     *
     * @param baseUrl the base URL to normalize
     * @return the normalized base URL
     */
    public static String normalizeUrl(String baseUrl) {
        String url = baseUrl.trim();

        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1).trim();
        }

        return url;
    }

    /**
     * Normalizes a path segment by trimming whitespace and removing leading and trailing slashes.
     *
     * @param path the path segment to normalize
     * @return the normalized path segment
     */
    public static String normalizePathSegment(String path) {
        String trimmed = path.trim();

        int start = trimmed.startsWith("/") ? 1 : 0;
        int end = trimmed.endsWith("/") ? trimmed.length() - 1 : trimmed.length();

        return trimmed.substring(start, end).trim();
    }

}
