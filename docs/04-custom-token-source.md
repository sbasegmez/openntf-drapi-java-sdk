# Writing a Custom Token Source

The bundled token sources cover one identity per `Drapi` instance. When different users need different identities, you write your own `TokenSource`. This page shows how, with two examples. Neither example ships with the SDK; copy and adapt them.

Read [Authentication](03-authentication.md) first. It describes the contract these examples implement.

## The shape of an implementation

`TokenSource` is sealed. Your class extends `TokenSourceBase`, whose constructor takes the `DrapiConfig` and the `HttpTransport`:

```java
public final class MyTokenSource extends TokenSourceBase {

    public MyTokenSource(DrapiConfig config, HttpTransport transport) {
        super(config, transport);
    }

    @Override public Token acquire(SessionContext session) { ... }
    @Override public void tokenRejected(SessionContext session, Token token) { ... }
    @Override public void logout(SessionContext session) { ... }
    @Override public boolean supportsRefresh() { ... }
}
```

The SDK creates the token source through a `TokenSourceProvider`. It has a single method, so a lambda works, and the lambda can pass in your own dependencies:

```java
Drapi drapi = Drapi.builder(config, (cfg, transport) -> new MyTokenSource(cfg, transport, myDependency))
                   .build();
```

The `transport()` available inside `TokenSourceBase` is the plain transport, without authentication. A request you send through it goes to the DRAPI base URL exactly as you built it, with no `Authorization` header added. This is what lets a token source call `/api/v1/auth` without looping back into itself.

## Rules every implementation must follow

- **Thread safety.** Several threads can call `acquire` for the same session at once. Either make sure only one of them fetches a new token (single flight), or accept that some fetches are duplicated.
- **No thread-local state.** `acquire` may run on an HTTP client thread during a retry. Read what you need from the `SessionContext`, never from `ThreadLocal`, `SecurityContextHolder` or a request-scoped bean.
- **Conditional discard.** In `tokenRejected`, discard your cached token only if its `bearer()` equals the rejected one. Compare the bearer strings, not the objects.
- **Cache.** `acquire` runs before every request. Return a cached token when you have a valid one.
- **Honest `supportsRefresh()`.** Return `true` only if a second `acquire` after `tokenRejected` can produce a different token. Otherwise the retry is wasted.

## Example 1: delegate a token the application already holds

Your web application signs users in through OAuth or OIDC and already has an access token for DRAPI. The SDK only needs to forward it.

The session context carries the token, captured on the request thread:

```java
public record OAuthSession(String sessionId, String username, String accessToken) implements SessionContext {

    @Override
    public String toString() {
        return "OAuthSession[username=" + username + "]"; // Keep the id and the token out of logs
    }
}
```

The token source passes it on:

```java
public final class DelegatedTokenSource extends TokenSourceBase {

    public DelegatedTokenSource(DrapiConfig config, HttpTransport transport) {
        super(config, transport);
    }

    @Override
    public Token acquire(SessionContext session) {
        if (session instanceof OAuthSession oauth) {
            return new Token(oauth.accessToken(), "oauth:" + oauth.username());
        }
        throw new IllegalStateException("Unsupported session type: " + session.getClass().getName());
    }

    @Override
    public void tokenRejected(SessionContext session, Token token) {
        // Nothing cached here. The application refreshes the token at its next sign-in check.
    }

    @Override
    public void logout(SessionContext session) {
        // The application owns the OAuth session and ends it through its identity provider.
    }

    @Override
    public boolean supportsRefresh() {
        return false; // A second acquire would return the same token
    }
}
```

Using it in a request handler:

```java
// Build once
Drapi drapi = Drapi.builder(config, DelegatedTokenSource::new).build();

// Per incoming request, on the request thread
OAuthSession session = new OAuthSession(httpSession.getId(), user.getName(), currentAccessToken());
DrapiClient client = drapi.forSession(session);
```

Because the context holds a token that expires, create a new client for each request rather than keeping one for the whole session.

## Example 2: log in to DRAPI on behalf of each user

Your server holds DRAPI credentials for several users, for example in a vault, and logs in as each one when needed. This is the multi-user version of `PasswordTokenSource`.

```java
public record UserSession(String sessionId, String username) implements SessionContext { }
```

```java
public final class PerUserPasswordTokenSource extends TokenSourceBase {

    private static final ApiPath AUTH_PATH = ApiPath.root("/auth");

    private final CredentialStore credentials; // Your type: returns the DRAPI password for a user
    private final ConcurrentHashMap<String, CompletableFuture<Token>> tokens = new ConcurrentHashMap<>();

    public PerUserPasswordTokenSource(DrapiConfig config, HttpTransport transport, CredentialStore credentials) {
        super(config, transport);
        this.credentials = credentials;
    }

    @Override
    public Token acquire(SessionContext session) {
        String user = session.username();

        // Single flight: the first thread for a user logs in, the others wait for its result
        CompletableFuture<Token> mine = new CompletableFuture<>();
        CompletableFuture<Token> existing = tokens.putIfAbsent(user, mine);
        if (existing != null) {
            return existing.join();
        }

        try {
            mine.complete(login(user));
        } catch (RuntimeException e) {
            tokens.remove(user, mine);
            mine.completeExceptionally(e);
            throw e;
        }
        return mine.join();
    }

    @Override
    public void tokenRejected(SessionContext session, Token rejected) {
        // Remove the cached token only if it is still the one the server refused
        tokens.computeIfPresent(session.username(), (user, future) -> {
            Token current = future.isDone() && !future.isCompletedExceptionally() ? future.join() : null;
            return current != null && current.bearer().equals(rejected.bearer()) ? null : future;
        });
    }

    @Override
    public void logout(SessionContext session) {
        tokens.remove(session.username());
    }

    @Override
    public boolean supportsRefresh() {
        return true;
    }

    private Token login(String user) {
        String body = JsonBinding.get().toJson(Map.of("username", user, "password", credentials.passwordFor(user)));

        DrapiRequest request = DrapiRequest.create(HttpMethod.POST, AUTH_PATH)
                                           .body(RequestBody.ofString("application/json", body));

        try (DrapiResponse response = transport().submit(request)) {
            if (!response.isSuccess()) {
                throw new AuthenticationException("Login failed for " + user, request, response);
            }
            Object bearer = JsonBinding.get().fromJson(response.bodyAsString()).get("bearer");
            return new Token(String.valueOf(bearer), "password:" + user);
        }
    }
}
```

Wiring it up:

```java
Drapi drapi = Drapi.builder(config, (cfg, transport) -> new PerUserPasswordTokenSource(cfg, transport, vault))
                   .build();

DrapiClient client = drapi.forSession(new UserSession(sessionId, "Jane Doe"));
```

What this sketch leaves out, and a production version needs:

- **Expiry.** It keeps a token until the server refuses it. Read the expiry from the `/auth` response and renew shortly before it.
- **A size limit.** The map grows with every user. Bound it and evict expired entries.
- **Credential handling.** Cache tokens, never passwords. Fetch the password only on a cache miss.

## Testing your token source

- Test `acquire`, `tokenRejected` and `logout` directly. They are plain method calls.
- Test the race in `tokenRejected`: cache token A, replace it with token B, then report A as rejected. B must survive.
- For code that only needs *some* token source, mock `TokenSourceBase` rather than `TokenSource`. The interface is sealed.
