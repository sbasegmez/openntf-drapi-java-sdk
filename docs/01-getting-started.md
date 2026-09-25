# Getting Started

This guide takes you from an empty project to reading a document and a view from a Domino REST API server. It uses the bundled password token source, which suits command-line tools, tests and proofs of concept. For multi-user applications, read [Authentication](03-authentication.md) once you have this working.

## 1. Prepare the server

You need:

- A DRAPI server URL, for example `https://drapi.example.com:8880`
- A user name and password (or internet password) that can log in to DRAPI
- A scope (the SDK calls it a data source) pointing at your database, with a schema that exposes the forms and views you want to read

The samples use the [OpenNTF Projects Dataset](https://www.openntf.org/main.nsf/project.xsp?r=project/OpenNTF+Projects+Dataset) with a scope named `projectdb` and the `projects` view enabled.

## 2. Add the dependency

```xml
<dependency>
    <groupId>org.openntf.drapi</groupId>
    <artifactId>drapi-sdk-starter-jakarta</artifactId>
    <version>0.2.0-SNAPSHOT</version>
</dependency>
```

If your project uses JPMS, add `requires org.openntf.drapi;` to your `module-info.java`. The JSON module (`org.openntf.drapi.json.jakarta`) must be on the module path, but you do not `require` it. The SDK finds it as a service.

## 3. Create the configuration

The only required setting is the server's base URL:

```java
DrapiConfig config = DrapiConfig.builder()
                                .baseUrl("https://drapi.example.com:8880")
                                .build();
```

You can also load it from a properties file or environment variables. See [Configuration](02-configuration.md).

## 4. Build the Drapi instance

```java
String password = ...; // From a prompt, a secret store or an environment variable. Not hard-coded.

Drapi drapi = Drapi.builder(config, PasswordTokenSourceProvider.withCredentials("Jane Doe", password))
                   .build();
```

`Drapi` is the application-wide object. Build it once and keep it. The second argument decides how the SDK obtains bearer tokens. `PasswordTokenSourceProvider` logs in to `/api/v1/auth` with the user name and password you give it. To request specific scopes, use `withCredentials("Jane Doe", password, "$DATA")`.

## 5. Open a client for a session

```java
DrapiClient client = drapi.forSession(SessionContext.singleUser());
```

A `DrapiClient` represents one user session. `SessionContext.singleUser()` is enough when the whole application runs as one user.

## 6. Read a document

```java
DrapiDataSource ds = client.dataSource("projectdb");

ds.documents()
  .get(unid, DocumentsGetOptions.create().meta(true))
  .thenAccept(doc -> {
      String name = doc.field("name").asString().orElse("-");
      System.out.println("Project: " + name);
  })
  .join();
```

Every API call returns a `CompletableFuture`. Call `join()` when you want to wait for the result.

## 7. Read view entries

`lists().get(...)` returns a `Stream<ListEntry>`. The stream holds the HTTP response open, so always close it, for example with try-with-resources:

```java
ds.lists()
  .get("projects", ListsGetOptions.create().count(20))
  .thenAccept(entries -> {
      try (entries) {
          entries.forEach(e -> System.out.println(e.column("name").asString().orElse("-")));
      }
  })
  .join();
```

A short-circuiting operation such as `findFirst()` does not consume the whole stream, so the close is what releases the connection.

## 8. Handle errors

Failures arrive through the future. When you call `join()`, they are wrapped in a `CompletionException`:

```java
try {
    Document doc = ds.documents().get(unid).join();
} catch (CompletionException e) {
    if (e.getCause() instanceof AuthenticationException) {
        // Login failed or the token was refused
    } else if (e.getCause() instanceof DrapiException de) {
        // The server returned an error. de carries the method, path and HTTP status.
    }
}
```

In a chain, use `.exceptionally(...)` or `.handle(...)` and unwrap the cause in the same way.

## 9. Clean up

```java
client.logout();   // Tells the token source the session is over
drapi.shutdown();  // Stops the HTTP transport
```

## Next steps

- [Configuration](02-configuration.md): all settings and where they can come from
- [Authentication](03-authentication.md): sessions, token sources and multi-user applications
- [Samples](../drapi-sdk-samples/README.md): runnable versions of the code above
