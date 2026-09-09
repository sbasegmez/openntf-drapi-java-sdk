# OpenNTF Java SDK for HCL Domino REST API

The OpenNTF Java SDK for HCL Domino REST API is a Java library that provides a convenient way to interact with the HCL Domino REST API. It simplifies making HTTP requests, handling responses, and working with JSON data.

HCL provides SDK implementations for Go and Node.js, which can be found in the [OpenNTF Domino REST API SDKs](https://opensource.hcltechsw.com/Domino-rest-api/references/sdk.html). This Java SDK is intended for Java applications that need to communicate with HCL Domino servers via the REST API and is designed to be simple, lightweight, and easy to use.

This SDK targets Java developers building applications that need to interact with HCL Domino servers. It provides a set of classes and methods that abstract the complexities of working with the REST API, allowing developers to focus on building their applications. The SDK is also designed to be compatible with various Java frameworks and libraries, making it easy to integrate into existing projects.

## Planned implementation

This SDK targets Java 17 and above, and is designed to be modular and extensible. The core implementation targets minimal/no dependencies, but additional modules may be provided to support specific frameworks or libraries. The SDK is designed to be easy to use and understand, with clear documentation and examples provided.

Planned SDK surface is documented in the [API Semantics](docs/api-semantics.md) document. You might check [drapi-sdk-samples](drapi-sdk-samples/README.md) for usage examples.

## Documentation

- [Configuration](docs/configuration.md)
- [Development](docs/development.md)
- [Api Semantics](docs/api-semantics.md)

## JSON Serialization

The SDK uses a JSON abstraction and it does not depend on a specific Json library. The initial provide Jakarta Json API (Json-b and Json-p) implementation for serialization and deserialization, but the SDK is designed to be flexible and allow for other JSON libraries to be used if desired. Jackson support is also planned in a later stage.

## Adding the SDK to your project

You can add the SDK to your project using Maven. You can include the following dependency in your `pom.xml` file:

```xml
<dependency>
    <groupId>com.openntf.drapi</groupId>
    <artifactId>drapi-sdk-starter-jakarta</artifactId>
    <version>0.1.0</version>
</dependency>
```

This will include the core SDK and the Jakarta JSON implementation. As additional libraries are added, they will be provided as separate dependencies, allowing you to choose the ones that best fit your platform.

Pre-release versions of the SDK are available in the [OpenNTF Maven Repository](https://artifactory.openntf.org/). You can add the repository to your `pom.xml` file as follows:

```xml
 <repositories>
    <repository>
        <id>artifactory.openntf.org</id>
        <name>artifactory.openntf.org</name>
        <url>https://artifactory.openntf.org/openntf</url>
    </repository>
</repositories>
```

## License

This project is licensed under the Apache License 2.0.
