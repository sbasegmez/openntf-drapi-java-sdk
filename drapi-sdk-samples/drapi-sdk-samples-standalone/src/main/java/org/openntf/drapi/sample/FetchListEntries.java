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
import java.util.List;
import java.util.stream.Stream;
import org.openntf.drapi.DrapiClient;
import org.openntf.drapi.DrapiConfig;
import org.openntf.drapi.DrapiDataSource;
import org.openntf.drapi.api.options.ListsGetOptions;
import org.openntf.drapi.meta.Column;
import org.openntf.drapi.meta.ListEntry;

public class FetchListEntries {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    public static void main(String[] args) {
        // Create the configuration for the DRAPI client. You can use the template file "config/drapi-sdk-sample.properties" to configure your DRAPI server connection.
        DrapiConfig config = DrapiConfig.builder()
                                        .applyResourceFile("config/drapi-sdk-sample.properties")
                                        .build();

        DrapiClient client = DrapiClient.builder(config)
                                        .build();

        // Create a scope on your favourite DRAPI server and name it as "projectdb".
        DrapiDataSource ds = client.dataSource("projectdb");

        // Fetch the list entries from the "projects" view, limiting to 20 entries and excluding metadata.
        // Do not forget to enable "projects" view in your schema.
        ds.lists()
          .get("projects", ListsGetOptions.create().count(20).meta(false))
          .thenAccept(FetchListEntries::consumeListStream)
          .exceptionally(FetchListEntries::handleError)
          .join(); // Join the CompletableFuture to ensure the flow waits for completion before proceeding.

    }

    // Consume the stream of ListEntry objects and print their details in a formatted manner.
    private static void consumeListStream(Stream<ListEntry> listStream) {
        // Ensure the stream is closed after processing
        try (listStream) {
            listStream.forEach(FetchListEntries::printListEntry);
        }
    }

    // Print the details of a single ListEntry in a formatted manner.
    private static void printListEntry(ListEntry entry) {
        // Extract the "name" column value, defaulting to "--Unknown project--" if not present
        String name = entry.column("name")
                           .asString()
                           .orElse("--Unknown project--");

        // Extract the "created" column value, defaulting to "--Unknown date--" if not present
        String created = entry.column("created")
                              .asDateTime()
                              .map(OffsetDateTime::toZonedDateTime)
                              .map(dt -> dt.format(FORMATTER))
                              .orElse("--Unknown date--");

        // Extract the "Chefscooks" column value, handling both single and multi-value cases
        Column col = entry.column("Chefscooks");
        List<String> chefscooks = col.isMultiValue() ? col.asList(String.class) : List.of(col.asString()
                                                                                             .orElse("--No chefs--"));

        System.out.printf("%1$-45s %2$-30s %3$s%n", name, created, String.join(", ", chefscooks));
    }

    private static Void handleError(Throwable ex) {
        Throwable cause = ex;

        if (ex instanceof java.util.concurrent.CompletionException && ex.getCause() != null) {
            cause = ex.getCause(); // Unwrap the CompletionException to get the actual cause
        }

        // Do something with the exception, e.g., log it or print the stack trace

        System.out.println("An error occurred while fetching the document: " + cause.getMessage());
        cause.printStackTrace();

        // Satisfy CompletableFuture<Void> return type by returning null
        return null;
    }


}
