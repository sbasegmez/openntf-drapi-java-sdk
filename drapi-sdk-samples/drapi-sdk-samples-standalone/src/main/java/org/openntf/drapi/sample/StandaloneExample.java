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

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.openntf.drapi.DrapiClient;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.DrapiDataSource;
import org.openntf.drapi.api.options.DocumentsGetOptions;
import org.openntf.drapi.api.options.ListsGetOptions;
import org.openntf.drapi.meta.Column;
import org.openntf.drapi.meta.Document;
import org.openntf.drapi.meta.ListEntry;
import org.openntf.drapi.util.TypeUtils;

public class StandaloneExample {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    public static void main(String[] args) {
        DrapiConfig config = DrapiConfig.builder()
                                        .applyResourceFile("config/drapi-sdk-sample.properties")
                                        .build();

        DrapiClient client = DrapiClient.builder(config)
                                        .build();

        // Create a scope on your favourite DRAPI server and name it as "projects".
        DrapiDataSource ds = client.dataSource("projects");

        String unid = ds.lists()
                        .get("projects", ListsGetOptions.create().count(10).meta(true))
                        .thenApply(StandaloneExample::consumeListStream)
                        .join(); // Join the CompletableFuture to get the UNID

        System.out.println("-------------------------------");
        System.out.println("Fetching document with UNID: " + unid);
        System.out.println("-------------------------------");

        // Do not forget to create a schema for your scope. You can also change the UNID below to a valid UNID of a document in your db.
        ds.documents()
          .get(unid, DocumentsGetOptions.create().meta(true))
          .thenAccept(StandaloneExample::processDocument)
          .exceptionally(StandaloneExample::handleError)
          .join();

    }

    private static String consumeListStream(Stream<ListEntry> listStream) {
        List<String> unids = new ArrayList<>();

        try (listStream) {
            listStream.forEach(entry -> {
                entry.unid().ifPresent(unids::add);

                String name = entry.column("name")
                                   .asString()
                                   .orElse("--Unknown project--");

                String created = entry.column("created")
                                      .asDateTime()
                                      .map(OffsetDateTime::toZonedDateTime)
                                      .map(dt -> dt.format(FORMATTER))
                                      .orElse("--Unknown date--");

                Column col = entry.column("Chefscooks");

                List<String> chefscooks = col.isMultiValue() ? col.asList(String.class) : List.of(col.asString()
                                                                                                     .orElse("--No chefs--"));

                System.out.printf("%1$-30s %2$-45s %3$s%n", name, created, String.join(", ", chefscooks));
            });
        }

        return unids.get(0); // Return the first UNID for demonstration purposes
    }

    private static void processDocument(Document document) {
        // Do something with the document, e.g., print its fields

        String projectName = document.field("name").asString().orElse("--Unknown project--");
        String projectOverview = document.field("overview").asString().orElse("--No overview--");

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

        // Do something with the exception, e.g., log it or print the stack trace

        ex.printStackTrace();

        // Satisfy CompletableFuture<Void> return type by returning null
        return null;
    }


}
