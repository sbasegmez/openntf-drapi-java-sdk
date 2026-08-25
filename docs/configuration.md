# Configuration

## Programatic Configuration

Simplest way to configure the application is to use the `DrapiConfig` class. You can create an
instance of this class and set the desired properties programmatically.

```java
DrapiConfig config = DrapiConfig.builder()
        .baseUrl("https://demo.example.com:8889")
        .basicAuth("Doctor notes", password) // Automatically switch to default Jwt authentication
        .userAgent("MyApp/1.0")  // Optional, if not set, default user agent will be used
        .connectTimeout(Duration.ofSeconds(2)) // Optional, default is 5 seconds.
        .requestTimeout(10) // Optional, default is 15 seconds.
        .authScope("$DATA") // Optional, DRAPI sets "MAIL $DATA" as default. 
        .build();
```

## Properties File

Alternatively, you can configure the application using a properties file. Create a file named `drapi.properties` in your classpath and set the desired properties.

Remember that the properties file should be in the classpath of your application. Keys are case 
insensitive, but values are case sensitive. The following is an example of a properties file:

```properties
BASEURL=https://api.example.com
AUTHSCOPE=$DATA

# This will set the authentication method to basic JWT authentication.
UserName=your_username
Password=your_password

# Alternatively you can supply Token for JWT authentication.
# Token=your_token

# Alternatively you can supply a OAuth information for JWT authentication.
# APPID=your_app_id
# APPSECRET=your_app_secret

USERAGENT=your_user_agent
CONNECTTIMEOUTSECS=13

# This should be ignored by the SDK, as it is not a valid property.
REQUESTTIMEOUTSECS=ignore
```

Then you can load the configuration from the properties file using the `DrapiConfig` class.

```java
DrapiConfig config = DrapiConfig.builder()
                                .applyResourceFile("drapi.properties")
                                .build();
```

You can also use a different file and provide File object to the `applyResourceFile` method.

```java
DrapiConfig config = DrapiConfig.builder()
                                .applyFile("/Users/dalek/invasion-plans.properties")
                                .build();
```

## Environment Variables

Finally, you can configure the application using environment variables. You may want to use a prefix for your environment variables to avoid conflicts with other applications. The `applyEnvironmentVariables` method allows you to specify a prefix for the environment variables.

```java
DrapiConfig config = DrapiConfig.builder()
                                .applyEnvironmentVariables("DRAPI_") 
                                .build();
```

The following environment variables are supported (assuming the prefix `DRAPI_`):

- `DRAPI_BASEURL`: The base URL of the API.
- `DRAPI_AUTHSCOPE`: The authentication scope.
- `DRAPI_USERNAME`: The username for basic authentication.
- `DRAPI_PASSWORD`: The password for basic authentication.
- `DRAPI_TOKEN`: The token for JWT authentication.
- `DRAPI_APPID`: The application ID for OAuth authentication.
- `DRAPI_APPSECRET`: The application secret for OAuth authentication.
- `DRAPI_USERAGENT`: The user agent string.
- `DRAPI_CONNECTTIMEOUTSECS`: The connection timeout in seconds.
- `DRAPI_REQUESTTIMEOUTSECS`: The request timeout in seconds.

## Notes

- Authentication method will be inferred based on the provided credentials. If both 
  username/password and token are provided, the SDK will use the default JWT authentication. If 
  token is given, it will inject token automatically into the Authorization header. If OAuth credentials are provided, it will use OAuth authentication.
- If you provide both username/password and OAuth credentials, the SDK will fail with `IllegalArgumentException` because it cannot determine which authentication method to use.
