# Configuration

`DrapiConfig` holds the connection settings for a `Drapi` instance. It is immutable once built. You create it with `DrapiConfig.builder()` and pass it to `Drapi.builder(...)`.

The configuration holds no credentials of its own. It has a few typed core settings and a set of extension parameters. Credentials usually go straight to the token source provider (see [Authentication](03-authentication.md)); extension parameters are for extensions that read their settings from the configuration.

## Core settings

| Setting             | Builder method                         | Key (file / environment) | Required | Default                          |
|---------------------|----------------------------------------|--------------------------|----------|----------------------------------|
| Base URL            | `baseUrl(String)`, `baseUrl(URI)`      | `baseurl`                | Yes      | None                             |
| User agent          | `userAgent(String)`                    | `useragent`              | No       | `OPENNTF-DRAPI-SDK-JAVA/<version>` |
| Connect timeout     | `connectTimeout(int)`, `connectTimeout(Duration)` | `connecttimeoutsecs` | No  | 5 seconds                        |
| Request timeout     | `requestTimeout(int)`, `requestTimeout(Duration)` | `requesttimeoutsecs` | No  | 15 seconds                       |

The base URL is the server root, for example `https://drapi.example.com:8880`. The SDK adds `/api/v1/...` itself.

Timeouts must be greater than zero. A non-numeric timeout in a file or environment variable is logged as a warning and ignored.

## Extension parameters

Any key that is not a core setting is stored as an extension parameter. Keys are case-insensitive.

### Credentials for the bundled token sources

In code, pass credentials to the provider:

```java
PasswordTokenSourceProvider.withCredentials("Jane Doe", password)
FixedTokenSourceProvider.withToken(token)
```

When credentials come from a properties file or environment variables instead, use the no-argument providers (`new PasswordTokenSourceProvider()`, `new FixedTokenSourceProvider()`). They read these keys:

| Key             | Constant                             | Purpose                                                   |
|-----------------|--------------------------------------|-----------------------------------------------------------|
| `auth.username` | `PasswordTokenSource.USERNAME_KEY`   | User name for `/api/v1/auth`. Required.                   |
| `auth.password` | `PasswordTokenSource.PASSWORD_KEY`   | Password for `/api/v1/auth`. Required.                    |
| `auth.scope`    | `PasswordTokenSource.SCOPE_KEY`      | Scopes to request, for example `$DATA`. Optional. When omitted, the server default applies. |
| `auth.token`    | `FixedTokenSource.AUTH_TOKEN_KEY`    | A ready bearer token. Required.                           |

Values passed to `withCredentials` or `withToken` take priority. With `withCredentials`, none of the three `auth.*` keys above are read, including `auth.scope`.

### Setting extension parameters in code

```java
DrapiConfig config = DrapiConfig.builder()
                                .baseUrl("https://drapi.example.com:8880")
                                .addExtraParam(MyExtension.MAX_SESSIONS, 500) // a ConfigKey<Integer>, see below
                                .build();
```

`addExtraParam(String, Object)` and `addExtraParams(Map<String, Object>)` do the same with plain string keys.

### Reading extension parameters

An extension declares a `ConfigKey<T>` with a name, a type and an optional default, then reads it from the configuration:

```java
static final ConfigKey<Integer> MAX_SESSIONS = ConfigKey.of("myapp.maxsessions", Integer.class, 1000);

int max = config.get(MAX_SESSIONS).orElseThrow();
```

`get(String key, Class<T> type)` and `get(String key, Class<T> type, T defaultValue)` do the same without a `ConfigKey`.

Values from files and environment variables arrive as strings. `get` converts them to the requested type. Supported types are `String`, `Integer`, `Long`, `Double`, `Boolean`, `OffsetDateTime` and `LocalDate`. If a value cannot be converted, a warning is logged and the default (or an empty `Optional`) is returned.

## Sources

You can combine the three sources below in one builder. They are applied in the order you call them, and a later value replaces an earlier one for the same key.

```java
DrapiConfig config = DrapiConfig.builder()
                                .applyResourceFile("config/drapi.properties") // defaults
                                .applyEnvironmentVariables("DRAPI_")          // overrides from the environment
                                .userAgent("MyApp/1.0")                       // fixed in code
                                .build();
```

### Properties file

On the classpath:

```java
DrapiConfig.builder().applyResourceFile("config/drapi.properties");
```

On the file system:

```java
DrapiConfig.builder().applyFile(new File("/etc/myapp/drapi.properties"));
```

Example file:

```properties
# Required
BASEURL=https://drapi.example.com:8880

# Read by new PasswordTokenSourceProvider()
auth.username=Jane Doe
auth.password=change-me
auth.scope=$DATA

# Read by new FixedTokenSourceProvider() (use instead of the three lines above)
# auth.token=eyJhbGciOi...

# Optional
USERAGENT=MyApp/1.0
CONNECTTIMEOUTSECS=5
REQUESTTIMEOUTSECS=15
```

Keys are case-insensitive. Values are case-sensitive.

With JPMS, the module that contains the file must open the folder that holds it (for example `opens config;`). Otherwise `applyResourceFile` cannot see it and fails with `IllegalArgumentException: Resource not found`.

### Environment variables

```java
DrapiConfig.builder().applyEnvironmentVariables("DRAPI_");
```

Only variables starting with the prefix are read (the prefix match is case-insensitive). The name is then converted to a key in three steps:

1. The prefix is removed.
2. The rest is converted to lower case.
3. Every underscore becomes a dot.

| Environment variable       | Key                  |
|----------------------------|----------------------|
| `DRAPI_BASEURL`            | `baseurl`            |
| `DRAPI_CONNECTTIMEOUTSECS` | `connecttimeoutsecs` |
| `DRAPI_AUTH_USERNAME`      | `auth.username`      |
| `DRAPI_AUTH_PASSWORD`      | `auth.password`      |
| `DRAPI_AUTH_SCOPE`         | `auth.scope`         |
| `DRAPI_AUTH_TOKEN`         | `auth.token`         |

Because of step 3, a key read from the environment cannot contain an underscore.

## Keeping secrets safe

- Keep properties files that hold passwords or tokens out of version control.
- In code, pass credentials to `withCredentials` or `withToken` rather than storing them in the configuration.
- Prefer environment variables or a secret store in deployed applications.
- Extension parameters are stored as plain values, with no marking of which ones are secret. Do not log values you read from the configuration.
