# Authentication

Every DRAPI call needs a bearer token (a JWT) in the `Authorization` header. This page explains how the SDK gets that token, which parts you supply, and what happens when the server refuses a token.

## Principles

- **The SDK does not care where a token comes from.** It asks a `TokenSource` for a bearer and puts it on the request.
- **Nothing DRAPI-specific is built into the contract.** Logging in to `/api/v1/auth` with a password is one token source among others. An application that already holds an OAuth access token passes it through its own token source.
- **The SDK keeps no session state.** Caching, expiry and renewal belong to the token source.

## The pieces

```
Drapi  (application scope, built once)
 ├─ DrapiConfig
 ├─ HttpTransport
 └─ TokenSource ◄── created by the TokenSourceProvider you pass to Drapi.builder(...)

DrapiClient  (one per user session)
 ├─ Drapi
 └─ SessionContext ◄── implemented by your application

DrapiDataSource  (one per DRAPI scope)
 └─ DrapiClient
```

| Type                   | Package                     | Who implements it                                  |
|------------------------|-----------------------------|----------------------------------------------------|
| `SessionContext`       | `org.openntf.drapi.auth`    | Your application, or `SessionContext.singleUser()` |
| `Token`                | `org.openntf.drapi.auth`    | Record, created by token sources                   |
| `TokenSource`          | `org.openntf.drapi.auth`    | Bundled, or your class extending `TokenSourceBase` |
| `TokenSourceProvider`  | `org.openntf.drapi.auth`    | Bundled, or your own (a lambda is enough)          |

### SessionContext

Identifies one user session. It has two methods, and the SDK uses both only for logging:

- `String sessionId()`: stable for the life of the session
- `String username()`: shown in logs and error messages

A `SessionContext` must also carry everything your token source needs to produce a token for that session, because the token source receives nothing else. Rules for implementations:

- Do not hold a live container object such as an `HttpServletRequest`. Copy the values you need when you create the context.
- Do not read `ThreadLocal` state later. The context is created on one thread and used on others.
- Treat `sessionId` as a credential. Do not log it in full.

The SDK never uses a `SessionContext` as a map key, so your `equals` and `hashCode` do not matter to it.

### Token

A record with two components:

- `bearer()`: the value sent as `Authorization: Bearer <bearer>`. Must not be empty.
- `label()`: a description for logs, for example `"oauth:jane"`

`toString()` redacts the bearer, so a `Token` is safe to log.

### TokenSource

The contract between the SDK and whatever produces tokens:

| Method                                   | Called when                                      |
|------------------------------------------|--------------------------------------------------|
| `Token acquire(SessionContext)`          | Before every authenticated request               |
| `void tokenRejected(SessionContext, Token)` | The server answered 401 to a request that carried this token |
| `void logout(SessionContext)`            | The application calls `client.logout()`          |
| `boolean supportsRefresh()`              | On a 401, to decide whether a retry makes sense  |

`acquire` is called for every request, so it should return a cached token when it has one. It may block (the password source calls the server on a cache miss), but it should be quick.

`TokenSource` is a sealed interface. Your own implementation extends `TokenSourceBase`. See [Writing a Custom Token Source](04-custom-token-source.md).

### TokenSourceProvider

A factory with one method, `TokenSource create(DrapiConfig, HttpTransport)`. `Drapi.builder(config, provider).build()` calls it once, after the transport exists, so the token source receives the same configuration and transport as the rest of the SDK.

## How a request is authenticated

1. The API method builds a request and attaches the client's `SessionContext`.
2. The SDK calls `tokenSource.acquire(session)`.
   - If it returns `null`, the call fails with `AuthenticationException`.
   - If it throws `AuthenticationException`, the call fails with that exception.
   - If it throws anything else, the call fails with `AuthenticationException`, with the original exception as the cause.
3. The SDK sets the `Authorization: Bearer ...` header, replacing any existing value, and sends the request.
4. If the server answers **401** and `supportsRefresh()` returns `true`:
   1. The SDK calls `tokenRejected(session, token)` with the token that was refused.
   2. It calls `acquire` again and sends the request a second time.
   3. There is never more than one retry per request.
5. If the final answer is 401, the call fails with `AuthenticationException`. Any other status outside 2xx fails with `DrapiException`. A 403 is not treated as an authentication problem and is not retried.

Things to know about the retry:

- `tokenRejected` receives the refused token so that the token source can discard its cached token only if it is still the same one. If another thread has already replaced it with a working token, the working token survives.
- The second `acquire` usually runs on an HTTP client thread, not on the thread that made the call. This is why token sources must not rely on thread-local state.
- A retry sends the request body again. A body created with `RequestBody.ofStreaming(...)` must return a fresh stream on each call to its supplier.

## Bundled token sources

### PasswordTokenSource

```java
Drapi drapi = Drapi.builder(config, PasswordTokenSourceProvider.withCredentials("Jane Doe", password))
                   .build();

// With specific scopes
PasswordTokenSourceProvider.withCredentials("Jane Doe", password, "$DATA");
```

Logs in to `POST /api/v1/auth` with the given user name, password and optional scope. `withCredentials` rejects a blank user name or password with `IllegalArgumentException`.

If the credentials come from a properties file or environment variables, use `new PasswordTokenSourceProvider()` instead. It reads `auth.username`, `auth.password` and the optional `auth.scope` from the configuration (see [Configuration](02-configuration.md#credentials-for-the-bundled-token-sources)).

- Holds **one token for the whole `Drapi` instance**. It ignores the `SessionContext`, so every client built from that `Drapi` acts as the same user.
- Reuses the token until 30 seconds before it expires, then logs in again.
- Only one thread logs in at a time. Other threads wait and reuse the new token.
- `supportsRefresh()` is `true`. After a 401 it discards the token and logs in again.
- `logout` clears the cached token and calls `GET /api/v1/auth/logout` unless the token has already expired. A failed logout call is logged, not thrown.

Use it for command-line tools, scheduled jobs, integration tests and demos. It is not suitable for applications where different users need different identities.

### FixedTokenSource

```java
Drapi drapi = Drapi.builder(config, FixedTokenSourceProvider.withToken(token)).build();
```

Returns the given token for every request and every session. `withToken` rejects a blank token with `IllegalArgumentException`. To read the token from the configuration key `auth.token` instead, use `new FixedTokenSourceProvider()`.

- `supportsRefresh()` is `false`. A 401 fails immediately with `AuthenticationException`.
- `logout` does nothing, because the SDK did not create the token.

Use it when a token is handed to your process from outside, for example by a deployment pipeline.

### SessionContext.singleUser()

Returns a `SingleUserSessionContext` with a random session id and the user name `single-user`. Pair it with either bundled token source.

## Choosing an approach

| Situation                                               | Token source                      | Session context                    |
|---------------------------------------------------------|-----------------------------------|------------------------------------|
| CLI tool, batch job, test, demo                         | `PasswordTokenSourceProvider.withCredentials(...)` | `SessionContext.singleUser()` |
| Token supplied from outside, one identity               | `FixedTokenSourceProvider.withToken(...)`          | `SessionContext.singleUser()` |
| Web application, users signed in through OAuth or OIDC  | Your own                          | Your own, one per user session     |
| Server acting for many users with stored credentials    | Your own                          | Your own, one per user             |

Servlet and Spring support modules are planned. Until then, see [Writing a Custom Token Source](04-custom-token-source.md) for the multi-user cases.

## Lifecycle

```java
Drapi drapi = Drapi.builder(config, provider).build();   // once, at start-up

DrapiClient client = drapi.forSession(sessionContext);   // per session or per request
// ... API calls ...
client.logout();                                         // when the user's session ends

drapi.shutdown();                                        // once, at shut-down
```

- `forSession` only allocates a small object. You can keep the client for the length of a session or create one per request.
- `client.logout()` passes the client's session to `TokenSource.logout`. What happens next is up to the token source. With `PasswordTokenSource`, which shares one token, logging out one client logs out all clients of that `Drapi`; the next call logs in again.
- DRAPI has no token revocation endpoint. A DRAPI-issued JWT stays valid at the server until it expires, whatever the SDK does locally.
- `drapi.shutdown()` calls `stop()` on the HTTP transport. The default JDK transport has nothing to release yet, but call it anyway so that your code keeps working when that changes. If you passed your own executor to `Drapi.builder(...).httpExecutor(...)`, you are responsible for shutting it down.

## Errors

| Exception                  | Meaning                                                                 |
|----------------------------|-------------------------------------------------------------------------|
| `AuthenticationException`  | No token could be obtained, or the server answered 401 after any retry. Extends `DrapiException`. |
| `DrapiException`           | The server answered with another error status. Carries the HTTP method, path and status. |
| `HttpTransportException`   | Network-level failure while sending a request.                          |

With `CompletableFuture`, these arrive wrapped in `CompletionException` when you call `join()`. Check `getCause()`.

## Security notes

- `Token.toString()` redacts the bearer. Keep it that way in your own types: do not add bearers or passwords to `toString()` output.
- Do not log `SessionContext.sessionId()` in full. A servlet session id is itself a credential.
- `Drapi.transport()` returns the authenticating transport. It is public for now but is not meant for application code.
