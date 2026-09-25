# OpenNTF Java SDK for HCL Domino REST API

A Java library for the [HCL Domino REST API](https://opensource.hcltechsw.com/Domino-rest-api/index.html) (DRAPI). It handles HTTP, authentication and JSON so that your code works with documents and views rather than requests and responses.

HCL provides official SDKs for [Go and Node.js](https://opensource.hcltechsw.com/Domino-rest-api/references/sdk.html). This SDK fills the gap for Java applications.

## Status

The SDK is at an early stage. Version `0.2.0` (in development) replaces the authentication model of `0.1.0` and is not backwards compatible with it.

What works today:

- Reading a document: `ds.documents().get(unid)`
- Reading view and folder entries as a stream: `ds.lists().get(viewName)`
- Password authentication against `/api/v1/auth`, a fixed bearer token, or your own token source

The planned API surface is described in [API Semantics](docs/05-api-semantics.md).

## Requirements

- Java 17 or later
- A Domino REST API server

## Adding the SDK to your project

```xml
<dependency>
    <groupId>org.openntf.drapi</groupId>
    <artifactId>drapi-sdk-starter-jakarta</artifactId>
    <version>0.2.0-SNAPSHOT</version>
</dependency>
```

The starter brings in the core SDK, the Jakarta JSON binding and a working Jakarta JSON implementation (Parsson and Yasson).

Pre-release versions are published to the OpenNTF Maven repository:

```xml
<repositories>
    <repository>
        <id>artifactory.openntf.org</id>
        <name>artifactory.openntf.org</name>
        <url>https://artifactory.openntf.org/openntf</url>
    </repository>
</repositories>
```

## Quick start

```java
DrapiConfig config = DrapiConfig.builder()
                                .baseUrl("https://drapi.example.com:8880")
                                .build();

// Build once per application
Drapi drapi = Drapi.builder(config, PasswordTokenSourceProvider.withCredentials("Jane Doe", password))
                   .build();

// One client per user session. Cheap to create.
DrapiClient client = drapi.forSession(SessionContext.singleUser());

client.dataSource("projectdb")
      .documents()
      .get(unid)
      .thenAccept(doc -> System.out.println(doc.field("name").asString().orElse("-")))
      .join();

client.logout();
drapi.shutdown();
```

[Getting Started](docs/01-getting-started.md) walks through the same code step by step.

## Main types

| Type               | Scope               | Role                                                                    |
|--------------------|---------------------|-------------------------------------------------------------------------|
| `Drapi`            | Application         | Holds configuration, HTTP transport and the token source. Build once.   |
| `DrapiClient`      | User session        | Binds a `SessionContext` to the `Drapi` instance. Create per session.   |
| `DrapiDataSource`  | DRAPI scope         | Entry point to documents, lists and other APIs for one scope.           |
| `SessionContext`   | User session        | Identifies the session. Implemented by your application.                |
| `TokenSource`      | Application         | Supplies the bearer token for a session. Bundled or your own.           |

## Documentation

1. [Getting Started](docs/01-getting-started.md)
2. [Configuration](docs/02-configuration.md)
3. [Authentication](docs/03-authentication.md)
4. [Writing a Custom Token Source](docs/04-custom-token-source.md)
5. [API Semantics](docs/05-api-semantics.md)
6. [Development](docs/06-development.md)

See also the [Samples](drapi-sdk-samples/README.md).

## JSON serialisation

The core module does not depend on a JSON library. It finds a `JsonBindingProvider` through `ServiceLoader` at runtime. The Jakarta JSON-P/JSON-B binding is available now; a Jackson binding is planned.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) and the [AI Use Policy](AI_Use_Policy.md).

## License

Apache License 2.0. See [LICENSE](LICENSE).
