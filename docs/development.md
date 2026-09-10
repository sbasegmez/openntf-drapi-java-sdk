# Development

## Prerequisites

- Java 17 or later
- Maven 3.0.1 or later

## Getting Started

Clone the repository and build the whole reactor from the root:

```bash
git clone https://github.com/sbasegmez/openntf-drapi-java-sdk.git
cd openntf-drapi-java-sdk
mvn clean verify
```
## Module Layout

- `drapi-sdk-core`: Json-agnostic API surface: `DrapiClient`, `DrapiDataSource`, configuration, 
  authentication.
- `drapi-sdk-json-jakarta`: JSON binding for the SDK using Jakarta JSON-P/JSON-B. Declares the 
  Jakarta APIs as `provided`, so you supply your own implementation.
- `drapi-sdk-starter-jakarta`: a convenience dependency that pulls in `core`, `json-jakarta`, 
  and a working Jakarta implementation (Parsson/Yasson) together. This is what most consumers should add to their own project.
- `drapi-sdk-samples`: runnable examples, see [its README](../drapi-sdk-samples/README.md).
- `drapi-sdk-test`: integration tests against a real or mocked Domino REST API server.

## Testing

This project uses Mockito for mock testing. Maven is configured to run tests automatically during the build process.

If you are going to run tests manually (or on your IDE), do not forget to add Mockito agent to your VM options. You can do this by adding the following line to your VM options:

```
-javaagent:/path/to/mockito-core-5.23.0.jar
```

You can directly use Maven repository for this option:

```
-javaagent:/Users/<your-username>/.m2/repository/org/mockito/mockito-core/5.23.0/mockito-core-5.23.0.jar
```

Note that the version of Mockito may change over time, so make sure to use the correct version that matches your project's dependencies.

## Running the Samples

See [drapi-sdk-samples](../drapi-sdk-samples/README.md) for setup and a description of each runnable example.
