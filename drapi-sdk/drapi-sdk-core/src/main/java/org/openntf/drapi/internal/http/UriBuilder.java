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
package org.openntf.drapi.internal.http;

import java.net.URI;
import java.util.List;
import org.openntf.drapi.http.ApiPath;
import org.openntf.drapi.util.Parameter;
import org.openntf.drapi.util.TypeUtils;
import org.openntf.drapi.util.UriUtils;

public class UriBuilder {

    private final String baseUrl;
    private final StringBuilder queryString;

    private String encodedPath;

    private UriBuilder(String baseUrl) {
        this.baseUrl = UriUtils.validateBaseUrl(baseUrl);
        this.queryString = new StringBuilder();
    }

    public static UriBuilder startWith(URI base) {
        return new UriBuilder(base.toString());
    }

    public static UriBuilder startWith(String baseUrl) {
        return new UriBuilder(baseUrl);
    }

    public UriBuilder setPath(String path, boolean encode) {
        this.encodedPath = encode ? ApiPath.of(path).toString() : path;

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
        // We will allow empty values, but not empty keys. If the key is empty, we will ignore the parameter.
        if (TypeUtils.isNotBlank(key)) {
            if (!queryString.isEmpty()) {
                queryString.append("&");
            }
            queryString.append(UriUtils.encodeQueryParam(key))
                       .append("=")
                       .append(value==null ? "" : UriUtils.encodeQueryParam(value)); // Nulls to empty strings
        }
        return this;
    }

    public URI build() {
        String path = TypeUtils.isNotBlank(encodedPath) ? ("/" + UriUtils.normalizePathSegment(encodedPath)) : "";
        String uriString = this.baseUrl + path + (queryString.isEmpty() ? "" : "?" + queryString);
        return URI.create(uriString);
    }

}
