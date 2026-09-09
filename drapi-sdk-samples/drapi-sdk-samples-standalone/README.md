# Drapi SDK Samples - Standalone Java Application

This directory contains sample code demonstrating how to use the Drapi SDK in a standalone Java application to interact with the HCL Domino REST API. Each sample below is a separate, runnable class with its own `main` method, focused on one use case.

## Setup and Configuration

Before running any of the samples, copy `src/main/resources/config/drapi-sdk-template.properties` to `src/main/resources/config/drapi-sdk-sample.properties` in this module, then edit the copy with your server details: set `BASEURL` to your DRAPI server's base URL, and provide either `UserName`/`Password` for basic authentication or a `Token` if you already have one.

The `drapi-sdk-sample.properties` file is excluded from version control (see `.gitignore`), so your credentials stay local. At startup, `DrapiConfig.builder().applyResourceFile("config/drapi-sdk-sample.properties")` loads this file from the classpath, so it must be under `src/main/resources` before you build or run a sample.

For a ready-made database to test against, you can use the [OpenNTF Projects Dataset](https://www.openntf.org/main.nsf/project.xsp?r=project/OpenNTF+Projects+Dataset). It already matches the `name`, `overview`, and `chefs` fields these samples read. Set it up as a DRAPI scope named `projectdb`, matching each sample's `client.dataSource("projectdb")` call, with a `projects` view enabled in its schema.

## Samples

Run any class below directly from your IDE, or from the command line with:

```bash
mvn exec:java -Dexec.mainClass=org.openntf.drapi.sample.<ClassName>
```

### FetchListEntries

Fetches entries from the `projects` view, streams the results, and reads typed column values, including a multi-value column via `Column.isMultiValue()`/`asList()`. Shows the correct pattern for consuming and closing a `Stream<ListEntry>`, and for handling errors on a `CompletableFuture` chain with `.exceptionally(...)`.

### FetchSingleDocument

Looks up a document's UNID by filtering the `projects` view for a specific project name, then fetches the full document and reads its typed fields via `Document.field(...)`, including iterating over the remaining fields dynamically. Builds on the lookup pattern from `FetchListEntries` to show a realistic find-then-fetch flow.

