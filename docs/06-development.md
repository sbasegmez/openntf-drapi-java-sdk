# Development

## Prerequisites

- Java 17 or later
- Maven 3.0.1 or later (enforced by the build)

## Building

Clone the repository and build the whole reactor from the root:

```bash
git clone https://github.com/sbasegmez/openntf-drapi-java-sdk.git
cd openntf-drapi-java-sdk
mvn clean verify
```

The `release` profile adds source and Javadoc jars, GPG signing and the licence header check:

```bash
mvn clean verify -Prelease
```

## Module layout

| Module                               | Purpose                                                                                     |
|--------------------------------------|---------------------------------------------------------------------------------------------|
| `drapi-sdk-parent`                   | Shared plugin and dependency configuration                                                  |
| `drapi-sdk/drapi-sdk-core`           | The SDK itself: `Drapi`, `DrapiClient`, configuration, authentication, HTTP, data APIs. No JSON library. |
| `drapi-sdk/drapi-sdk-json-jakarta`   | JSON binding on Jakarta JSON-P/JSON-B. Declares the Jakarta APIs as `provided`, so you supply an implementation. |
| `drapi-sdk/drapi-sdk-starter-jakarta`| Convenience dependency: core, the Jakarta binding and Parsson/Yasson. What most consumers add. |
| `drapi-sdk-samples`                  | Runnable examples. See [its README](../drapi-sdk-samples/README.md).                         |
| `drapi-sdk-test`                     | Integration tests against a mocked DRAPI server                                             |

## Package layout in drapi-sdk-core

| Package                              | Exported | Contents                                                             |
|--------------------------------------|----------|----------------------------------------------------------------------|
| `org.openntf.drapi`                  | Yes      | `Drapi`, `DrapiBuilder`, `DrapiClient`, `DrapiConfig`, `DrapiConfigBuilder`, `DrapiDataSource` |
| `org.openntf.drapi.auth`             | Yes      | `SessionContext`, `Token`, `TokenSource`, `TokenSourceBase`, `TokenSourceProvider` |
| `org.openntf.drapi.auth.builtin`     | Yes      | `PasswordTokenSource`, `FixedTokenSource`, their providers, `SingleUserSessionContext` |
| `org.openntf.drapi.api`              | Yes      | `DocumentsApi`, `ListsApi`                                           |
| `org.openntf.drapi.api.options`      | Yes      | Options objects and enums for the APIs                               |
| `org.openntf.drapi.http`             | Yes      | `HttpTransport`, `HttpTransportBase`, `HttpTransportProvider`, `DrapiRequest`, `DrapiResponse`, `RequestBody`, `ApiPath` |
| `org.openntf.drapi.json`             | Yes      | `JsonBinding`, `JsonBindingProvider` (service interface)             |
| `org.openntf.drapi.meta`             | Yes      | `Document`, `ListEntry`, `Field`, `Column` and related types         |
| `org.openntf.drapi.exception`        | Yes      | `DrapiException`, `AuthenticationException` and others               |
| `org.openntf.drapi.util`             | Yes      | `ConfigKey`, `ServiceRegistry`, helpers                              |
| `org.openntf.drapi.internal.*`       | No       | Implementations, including `AuthenticatingHttpTransport` and the JDK HTTP transport |

`org.openntf.drapi.internal.dto` is opened (not exported) so that the JSON binding can read and write the `/auth` request and response types.

## How authentication is wired

`DrapiBuilder.build()` creates the plain transport from the `HttpTransportProvider`, then the token source from the `TokenSourceProvider`, handing it the plain transport. `DrapiImpl` wraps the plain transport in `AuthenticatingHttpTransport`, which is what API calls go through. The token source therefore calls DRAPI without authentication and cannot recurse into itself.

The request flow and the 401 retry rules are described in [Authentication](03-authentication.md#how-a-request-is-authenticated).

## Service discovery

Two extension points are found through `ServiceLoader`:

- `JsonBindingProvider`: exactly one is required. The first JSON operation throws `ServiceConfigurationError` if none, or more than one, is found.
- `HttpTransportProvider`: optional. Falls back to the JDK `HttpClient` transport.

## Testing

Tests use JUnit 5 and Mockito. Maven runs them in the normal build and adds the Mockito agent automatically.

When you run tests from an IDE, add the Mockito agent to the VM options:

```
-javaagent:/Users/<your-username>/.m2/repository/org/mockito/mockito-core/5.23.0/mockito-core-5.23.0.jar
```

Check `mockito.version` in `drapi-sdk-parent/pom.xml` for the current version.

`TokenSource` and `HttpTransport` are sealed interfaces. In tests, mock `TokenSourceBase` and `HttpTransportBase` instead.

## Running the samples

See [drapi-sdk-samples](../drapi-sdk-samples/README.md) for setup and a description of each example.
