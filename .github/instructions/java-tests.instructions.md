---
applyTo: "**/src/test/java/**/*.java"
description: "How to write tests in the drapi-sdk repo (JUnit 5 + Mockito + in-repo HTTP mock server). Drives Copilot inline completion for test sources."
---

# Test authoring instructions (drapi-sdk)

These rules describe the existing test style in this repository. When completing or generating test code, match what is already here. Prefer copying the shape of a nearby test in the same package over inventing a new structure.

## Stack and commands

| Item             | Value                                                                                  |
| ---------------- | ------------------------------------------------------------------------------------- |
| Language level   | Java 17 (`--release 17`); use records, text blocks, `var`, switch expressions freely  |
| Test framework   | JUnit Jupiter (JUnit 5) `org.junit.jupiter.api.*`                                     |
| Assertions       | `org.junit.jupiter.api.Assertions` only — no AssertJ, no Hamcrest, no Truth           |
| Mocking          | Mockito 5 (`mockito-core`, `mockito-junit-jupiter`) — only where it earns its place   |
| HTTP mocking     | In-repo base classes in `org.openntf.drapi.internal.test` — never WireMock/MockWebServer |
| Run              | `mvn test`; single test: `mvn -pl drapi-sdk/drapi-sdk-core test -Dtest='ClassName#method'` |
| Coverage goal    | Aim for full coverage of new production code (see `CONTRIBUTING.md`)                   |

## File layout and naming

- Test class name is `<ClassUnderTest>Test`, in the **same package** as the class under test, mirrored under `src/test/java`.
- Test classes and test methods are **package-private** (no `public`): `class FooTest {`, `void doesSomething() {`.
- Method names describe behaviour, not implementation: `getRequestSendsPathAndMethod`, `nonRepeatableAuthenticationProviderFailsAfter401`, `testTokenCacheInvalidateDoesNotUseEquals`. A leading `test` prefix is tolerated in older files but a plain behaviour name is preferred for new tests.
- Wrap code and comments (including Javadoc) at 136 columns. Assertion messages routinely push a single line close to that limit — do not pre-wrap them at 80/100/120.

## Test method structure

- Annotate with `@Test` and, in almost all cases, `@DisplayName("Full sentence describing the expected behaviour")`.
- Static-import each assertion method explicitly, one per line: `import static org.junit.jupiter.api.Assertions.assertEquals;`. Do not use `Assertions.*` wildcard imports in new files (a few legacy stubs still do).
- Arrange / act / assert, separated by blank lines. Keep one behaviour per test; multiple related assertions in one test are fine when they check the same behaviour.
- Pass an explanatory message as the **last** argument to assertions whenever the failure would otherwise be ambiguous:
  `assertEquals("/create", request.path(), "The mirrored request path should match the original request path");`
- For exception tests, capture and inspect the thrown exception rather than only asserting its type:

    ```java
    DrapiException ex = assertThrowsExactly(DrapiException.class, () -> provider(config).acquireToken(toolkit(config)));
    assertEquals("Invalid authentication response", ex.getMessage(), "The exception message should explain the failure");
    ```
- Use `@Nested` non-static inner classes with their own `@DisplayName` to group related cases (see `AbstractJsonBindingTest`).
- Lifecycle hooks are package-private `void`: `@BeforeEach void setUp()`, `@AfterEach void tearDown()`. Leave them empty rather than deleting when a base class or convention expects them.
- Use `"""` text blocks for JSON and other multi-line fixtures.
- Put shared fixtures in `src/test/resources` (for example `config/example.properties`, `META-INF/services/...` for SPI tests).

## Mockito

- Only mock collaborators with real behaviour and side effects (transports, `Responder`, `JsonBinding`, auth providers). Never mock records, value objects, `DrapiRequest`, `DrapiResponse`, `BearerToken`, config — construct the real thing.
- Enable Mockito with `@ExtendWith(MockitoExtension.class)` on the class and `@Mock` on fields. 
- Stub with `when(mock.call(...)).thenReturn(...)`; verify with `verify(mock).call(...)`. Use matchers `any()`, `anyString()`, `eq(...)` from `org.mockito.ArgumentMatchers`.
- `MockitoExtension` runs in strict-stubs mode: do not add stubbings a test does not use.
- Sequential responses: `when(responder.respond(any())).thenReturn(response(401, "..."), response(200, "Success"));`.

## HTTP interaction tests

Do not pull in an external HTTP mock library. Extend one of the in-repo base classes, which start a real `com.sun.net.httpserver.HttpServer` on `127.0.0.1:0` and shut it down after each test.

### `MockableHttpTest` (Mockito-friendly responder)

- Register a responder with `respondWith(Responder)`; a `Responder` returns `MockResponse(int statusCode, String body, Map<String,String> headers)`, built via `response(status, body)` / `response(status, body, headers)`.
- Typically `@Mock Responder responder;` plus `respondWith(responder)` in `@BeforeEach`, then stub `responder.respond(...)` per test. Used by `AuthenticatingHttpTransportTest`.

## Configuration and environment

- Maven Surefire injects `DRAPI_BASEURL` and `DRAPI_AUTH_TOKEN` for `DrapiConfigBuilderTest`.
- Guard environment-dependent tests with `@EnabledIfEnvironmentVariable(named = "DRAPI_BASEURL", matches = ".*")` (stacked annotations are used for multiple variables).
- Load config fixtures from the classpath: `DrapiConfig.builder().applyResourceFile("config/example.properties")`.

## Do not

- Do not introduce AssertJ, Hamcrest, Truth, WireMock, MockWebServer, JUnit Pioneer, or a new test dependency without it first being added to `drapi-sdk-parent/pom.xml`.
- Do not make test classes or test methods `public`.
- Do not mock records / value types / `DrapiRequest` / `DrapiResponse`.
- Do not leave a `DrapiResponse` unclosed.
- Do not use `Thread.sleep` for synchronisation except when deliberately simulating a slow collaborator (as in the token-cache concurrency test).
