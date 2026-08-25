# OpenNTF Java SDK for HCL Domino REST API

The OpenNTF Java SDK for HCL Domino REST API is a Java library that provides a convenient way to interact with the HCL Domino REST API. It simplifies making HTTP requests, handling responses, and working with JSON data.

HCL provides SDK implementations for Go and Node.js, which can be found in the [OpenNTF Domino REST API SDKs](https://opensource.hcltechsw.com/Domino-rest-api/references/sdk.html). This Java SDK is intended for Java applications that need to communicate with HCL Domino servers via the REST API and is designed to be simple, lightweight, and easy to use.

This SDK targets Java developers building applications that need to interact with HCL Domino servers. It provides a set of classes and methods that abstract the complexities of working with the REST API, allowing developers to focus on building their applications. The SDK is also designed to be compatible with various Java frameworks and libraries, making it easy to integrate into existing projects.

## Planned implementation

This SDK targets Java 17 and above, and is designed to be modular and extensible. The core implementation targets minimal/no dependencies, but additional modules may be provided to support specific frameworks or libraries. The SDK is designed to be easy to use and understand, with clear documentation and examples provided.

_(Class names and SDK structure are not final...)_

```java
// Build a DrapiConfig first
DrapiConfig config = DrapiConfig.builder()
        .baseUrl("https://demo.example.com:8880")
        .basicAuth("Doctor notes", password)
        .build();

// Build once and share: the client caches the bearer tokes, and refresh as needed.
DrapiClient client = DrapiClient.builder(config)
                                .build();

DrapiDocument contact = client.scope("demo")
                              .documents()
                              .get("0123456789ABCDEF0123456789ABCDEF")
                              .orElseThrow();

```

## Documentation

- [Configuration](docs/configuration.md)


## JSON Serialization

The SDK uses a JSON abstraction and it does not depend on a specific Json library. The initial provide Jakarta Json API (Json-b and Json-p) implementation for serialization and deserialization, but the SDK is designed to be flexible and allow for other JSON libraries to be used if desired. Jackson support is also planned in a later stage.

## License

This project is licensed under the Apache License 2.0.
