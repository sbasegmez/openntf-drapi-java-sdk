# Drapi SDK Samples - Standalone Java Application

Runnable examples for the Drapi SDK in a plain Java application. Each class has its own `main` method and covers one use case. All samples run as a single user with the bundled `PasswordTokenSource`.

## Setup

1. Copy `src/main/resources/config/drapi-sdk-template.properties` to `src/main/resources/config/drapi-sdk-sample.properties`.
2. Edit the copy:
   - `BASEURL`: your DRAPI server, for example `https://drapi.example.com:8880`
   - `auth.username` and `auth.password`: a user that can log in to DRAPI
   - `auth.scope` (optional): the scopes to request, for example `$DATA`

`drapi-sdk-sample.properties` is listed in `.gitignore`, so your credentials stay local. The samples load it from the classpath with `DrapiConfig.builder().applyResourceFile("config/drapi-sdk-sample.properties")`, so it must be under `src/main/resources` before you build or run.

The module's `module-info.java` contains `opens config;`. Without it, the SDK cannot read the file across the module boundary.

### Test data

The samples expect the [OpenNTF Projects Dataset](https://www.openntf.org/main.nsf/project.xsp?r=project/OpenNTF+Projects+Dataset):

- A DRAPI scope named `projectdb` pointing at the database
- The `projects` view enabled in the scope's schema
- The `name`, `overview` and `chefs` fields readable

## What every sample does

```java
DrapiConfig config = DrapiConfig.builder()
                                .applyResourceFile("config/drapi-sdk-sample.properties")
                                .build();

Drapi drapi = Drapi.builder(config, new PasswordTokenSourceProvider()).build();
DrapiClient client = drapi.forSession(SessionContext.singleUser());

// ... sample-specific work with client.dataSource("projectdb") ...

client.logout();
drapi.shutdown();
```

The samples read credentials from the properties file, so they use the no-argument `new PasswordTokenSourceProvider()`. In your own code you can pass them directly with `PasswordTokenSourceProvider.withCredentials(username, password)`.

## Running

From your IDE, run the class directly. From the command line:

```bash
mvn exec:java -Dexec.mainClass=org.openntf.drapi.sample.<ClassName>
```

## Samples

### FetchListEntries

Reads 20 entries from the `projects` view and prints typed column values, including a multi-value column read with `Column.isMultiValue()` and `asList()`. Shows how to close a `Stream<ListEntry>` and how to handle errors in a `CompletableFuture` chain with `.exceptionally(...)`.

### FetchSingleDocument

Finds a document's UNID by looking up a project name in the `projects` view, then reads the full document and prints its fields with `Document.field(...)`. Also lists the remaining fields dynamically.
