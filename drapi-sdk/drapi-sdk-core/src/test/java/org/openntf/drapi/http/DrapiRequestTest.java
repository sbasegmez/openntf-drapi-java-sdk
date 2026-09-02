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
package org.openntf.drapi.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openntf.drapi.http.RequestBody.Bytes;
import org.openntf.drapi.internal.http.ApiPath;

class DrapiRequestTest {

    @Test
    @DisplayName("No query parameters should result in an empty list, not null")
    void testNoQueryParameters() {
        DrapiRequest request = DrapiRequest.get("/test");

        assertNotNull(request.queryParams(), "Query parameters should not be null");
        assertTrue(request.queryParams().isEmpty(), "Query parameters should be empty when none are added");
    }

    @Test
    @DisplayName("No headers should result in an empty map, not null")
    void testNoHeaders() {
        DrapiRequest request = DrapiRequest.get("/test");

        assertNotNull(request.headers(), "Headers should not be null");
        assertTrue(request.headers().isEmpty(), "Headers should be empty when none are added");
    }

    @Test
    @DisplayName("Adding a single query parameter should be reflected in the request")
    void testSingleQueryParameter() {
        DrapiRequest request = DrapiRequest.get("/test")
                                           .queryParam("key", "value");

        assertEquals(1, request.queryParams().size(), "There should be one query parameter");
        assertEquals("key", request.queryParams().get(0).key(), "Query parameter key should match");
        assertEquals("value", request.queryParams().get(0).value(), "Query parameter value should match");
    }

    @Test
    @DisplayName("Adding a single header should be reflected in the request")
    void testSingleHeader() {
        DrapiRequest request = DrapiRequest.get("/test")
                                           .header("Content-Type", "application/json");

        assertEquals(1, request.headers().size(), "There should be one header");
        assertTrue(request.headers().containsKey("Content-Type"), "Headers should contain 'Content-Type'");
        assertEquals("application/json", request.headers().get("Content-Type").get(0), "Header value should match");
    }

    @Test
    @DisplayName("Adding multiple headers with the same key should be reflected in the request")
    void testMultipleHeadersSameKey() {
        DrapiRequest request = DrapiRequest.get("/test")
                                           .header("Accept", "application/json")
                                           .header("Accept", "application/xml");

        assertEquals(1, request.headers().size(), "There should be one header key");
        assertTrue(request.headers().containsKey("Accept"), "Headers should contain 'Accept'");
        assertEquals(2, request.headers().get("Accept").size(), "There should be two values for 'Accept' header");
        assertTrue(request.headers().get("Accept").contains("application/json"), "'Accept' header should contain 'application/json'");
        assertTrue(request.headers().get("Accept").contains("application/xml"), "'Accept' header should contain 'application/xml'");
    }

    @Test
    @DisplayName("Adding multiple query parameters with the same key should be reflected in the request")
    void testMultipleQueryParametersSameKey() {
        DrapiRequest request = DrapiRequest.get("/test")
                                           .queryParam("key", "value1")
                                           .queryParam("key", "value2");

        assertEquals(2, request.queryParams().size(), "There should be two query parameters");
        assertEquals("key", request.queryParams().get(0).key(), "First query parameter key should match");
        assertEquals("value1", request.queryParams().get(0).value(), "First query parameter value should match");
        assertEquals("key", request.queryParams().get(1).key(), "Second query parameter key should match");
        assertEquals("value2", request.queryParams().get(1).value(), "Second query parameter value should match");
    }

    @Test
    @DisplayName("null values for query parameters should be ignored")
    void testNullQueryParameterValues() {
        DrapiRequest request = DrapiRequest.get("/test")
                                           .queryParam("key1", "value1")
                                           .queryParam("key2", null); // This should be ignored

        assertEquals(1, request.queryParams().size(), "There should be one query parameter");
        assertEquals("key1", request.queryParams().get(0).key(), "Query parameter key should match");
        assertEquals("value1", request.queryParams().get(0).value(), "Query parameter value should match");
    }

    @Test
    @DisplayName("null values for headers should be ignored")
    void testNullHeaderValues() {
        DrapiRequest request = DrapiRequest.get("/test")
                                           .header("Content-Type", "application/json")
                                           .header("Authorization", (String) null); // This should be ignored

        assertEquals(1, request.headers().size(), "There should be one header");
        assertTrue(request.headers().containsKey("Content-Type"), "Headers should contain 'Content-Type'");
        assertEquals("application/json", request.headers().get("Content-Type").get(0), "Header value should match");
    }

    @Test
    @DisplayName("Building a request with null method or path should throw an exception")
    void testBuildingRequestNullInputs() {
        assertThrows(NullPointerException.class, () -> DrapiRequest.create(HttpMethod.GET, (String) null));
        assertThrows(NullPointerException.class, () -> DrapiRequest.create(HttpMethod.GET, (ApiPath) null));
        assertThrows(NullPointerException.class, () -> DrapiRequest.create(null, "/test"));
    }

    @Test
    @DisplayName("No body should result in an empty body")
    void testNoBodyResultsInEmptyBody() {
        DrapiRequest request = DrapiRequest.get("/test");

        assertNotNull(request.body(), "Body should not be null");
        assertInstanceOf(Bytes.class, request.body(), "Body should be of type RequestBody.Bytes");
        assertEquals(0, ((RequestBody.Bytes) request.body()).data().length, "Body should be empty");
    }

    @Test
    @DisplayName("Header names should be case insensitive")
    void testCaseSensitivityForHeaderNames() {
        DrapiRequest request = DrapiRequest.get("/test")
                                           .header("Accept", "application/json")
                                           .header("ACCEPT", "application/xml");

        assertEquals(1, request.headers().size(), "There should be one header key");
        assertTrue(request.headers().containsKey("Accept"), "Headers should contain 'Accept'");
        assertTrue(request.headers().containsKey("accept"), "Headers should contain 'accept'");
        assertEquals(2, request.headers().get("ACCEPT").size(), "There should be two values for 'Accept' header");
        assertTrue(request.headers().get("Accept").contains("application/json"), "'Accept' header should contain 'application/json'");
        assertTrue(request.headers().get("Accept").contains("application/xml"), "'Accept' header should contain 'application/xml'");
    }

    @Test
    @DisplayName("Test paths working with string or ApiPath")
    void testPathsWithStringOrApiPath() {
        assertEquals("/test", DrapiRequest.get("/test").path(), "Path should match the string input");
        assertEquals("/test", DrapiRequest.get(ApiPath.of("/test")).path(), "Paths should be equal when using ApiPath");

        assertEquals("/test", DrapiRequest.patch("/test").path(), "Path should match the string input");
        assertEquals("/test", DrapiRequest.patch(ApiPath.of("/test")).path(), "Paths should be equal when using ApiPath");

        assertEquals("/test", DrapiRequest.post("/test").path(), "Path should match the string input");
        assertEquals("/test", DrapiRequest.post(ApiPath.of("/test")).path(), "Paths should be equal when using ApiPath");

        assertEquals("/test", DrapiRequest.put("/test").path(), "Path should match the string input");
        assertEquals("/test", DrapiRequest.put(ApiPath.of("/test")).path(), "Paths should be equal when using ApiPath");

        assertEquals("/test", DrapiRequest.delete("/test").path(), "Path should match the string input");
        assertEquals("/test", DrapiRequest.delete(ApiPath.of("/test")).path(), "Paths should be equal when using ApiPath");
    }

}
