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
package org.openntf.drapi.sample;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import org.openntf.drapi.DrapiClient;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.DrapiDataSource;
import org.openntf.drapi.api.options.DocumentsGetOptions;
import org.openntf.drapi.api.options.ListsGetOptions;
import org.openntf.drapi.meta.Document;
import org.openntf.drapi.meta.ListEntry;
import org.openntf.drapi.util.TypeUtils;

public class FetchSingleDocument {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    public static void main(String[] args) {
        DrapiConfig config = DrapiConfig.builder()
                                        .applyResourceFile("config/drapi-sdk-sample.properties")
                                        .build();

        DrapiClient client = DrapiClient.builder(config)
                                        .build();

        // Create a scope on your favourite DRAPI server and name it as "projectdb".
        DrapiDataSource ds = client.dataSource("projectdb");

        // Lookup unid of a document representing "XSnippets" project.
        Optional<String> unid = findDocumentUnid(ds, "XSnippets");

        if(unid.isEmpty()) {
            System.out.println("No document found for project 'XSnippets'. Please ensure the project exists in the 'projects' view.");
            return;
        }

        System.out.println("-----------------------------------------------------------------");
        System.out.println("Fetching document with UNID: " + unid.get());
        System.out.println("-----------------------------------------------------------------");

        // Do not forget to create a schema for your scope. You can also change the UNID below to a valid UNID of a document in your db.
        ds.documents()
          .get(unid.get(), DocumentsGetOptions.create().meta(true))
          .thenAccept(FetchSingleDocument::processDocument)
          .exceptionally(FetchSingleDocument::handleError)
          .join();

    }

    private static Optional<String> findDocumentUnid(DrapiDataSource ds, String projectName) {
        var options = ListsGetOptions.create()
                                     .meta(true) // Include metadata in the response, so we can access the UNID of the document
                                     .key(List.of(projectName)) // Filter by project name
                                     .keyAllowPartial(false); // Ensure we only get exact matches for the project name

        try {

            // Fetch the list entries from the "projects" view and extract the UNID of the first matching entry
            return ds.lists()
                     .get("projects", options)
                     .join() // Wait for the CompletableFuture to complete and get the Stream<ListEntry>
                     .findFirst()
                     .flatMap(ListEntry::unid);

        } catch (Exception ex) {
            handleError(ex);
            return Optional.empty();
        }
    }

    private static void processDocument(Document document) {
        // Do something with the document, e.g., print its fields

        // Extract the "name" and "overview" fields, defaulting to placeholders if not present
        String projectName = document.field("name").asString().orElse("--Unknown project--");
        String projectOverview = document.field("overview").asString().orElse("--No overview--");

        // Extract the "chefs" field as a list of strings, defaulting to an empty list if not present
        List<String> projectChefs = document.field("chefs").asList(String.class);

        System.out.println("Project Name: " + projectName);
        System.out.println("Project Overview: " + projectOverview);
        System.out.println("Thanks to Project Chefs: " + String.join(", ", projectChefs));

        System.out.println("------------------------------");
        System.out.println("Other fields in the project document:");

        document.fields()
                .filter(field -> !field.name().equals("name")) // Exclude the "name" field from printing
                .filter(field -> !field.name().equals("overview")) // Exclude the "overview" field from printing
                .filter(field -> !field.name().equals("chefs")) // Exclude the "chefs" field from printing
                .filter(field -> TypeUtils.isNotEmpty(field.raw().orElse(null))) // Only include fields that have a value
                .forEach(field -> System.out.println(" - " + field.name() + ": " + field.raw().orElse("--No value--")));

    }

    private static Void handleError(Throwable ex) {
        Throwable cause = ex;

        if (ex instanceof CompletionException && ex.getCause() != null) {
            cause = ex.getCause(); // Unwrap the CompletionException to get the actual cause
        }

        // Do something with the exception, e.g., log it or print the stack trace

        System.out.println("An error occurred while fetching the document: " + cause.getMessage());
        cause.printStackTrace();

        // Satisfy CompletableFuture<Void> return type by returning null
        return null;
    }


}
