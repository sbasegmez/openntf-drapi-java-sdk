package org.openntf.drapi.internal.auth;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.openntf.drapi.util.TypeUtils;

/**
 * A bearer token together with what the SDK knows about its claims.
 *
 * @param bearer the bearer token string
 * @param claims the claims associated with the token
 */
public record BearerToken(String bearer, Map<String, Object> claims) {

    private static final int DEFAULT_EXPIRY_SKEW = 30; // seconds

    public BearerToken {
        TypeUtils.requireNonEmpty(bearer, "bearer must not be null or empty");
        claims = claims == null ? Map.of() : Map.copyOf(claims);
    }

    /**
     * Reports whether the token should be replaced now.
     * <p>
     * For expiry, we'll use the "exp" claim if present. RFC 7519 defines "exp" as optional. So, absent other rules, a token without
     * "exp" has no JWT-defined expiration time and could remain acceptable indefinitely.
     *
     * @return whether the token needs replacing
     */
    public boolean isExpired() {
        return isExpired(DEFAULT_EXPIRY_SKEW);
    }

    /**
     * Reports whether the token should be replaced now.
     * <p>
     * For expiry, we'll use the "exp" claim if present. RFC 7519 defines "exp" as optional. So, absent other rules, a token without
     * "exp" has no JWT-defined expiration time and could remain acceptable indefinitely.
     * <p>
     * Keep in mind, though, that this "exp" rule is only valid for JWTs. Other token types may have different rules, and the server may
     * have its own rules that are not reflected in the token. So isExpired() is a best-effort check, not a guarantee that the token is
     * still valid.
     *
     * @param skew how far ahead of the real expiry to renew, covering clock drift and the request's own flight time
     * @return whether the token needs replacing
     */
    public boolean isExpired(int skew) {
        return getExpiresAt().map(expiry -> Instant.now().isAfter(expiry.minusSeconds(skew)))
                             .orElse(false);
    }

    /**
     * Returns the expiry time of the token, if known.
     *
     * @return the expiry time of the token, if known
     */
    private Optional<Instant> getExpiresAt() {
        Object expClaim = claims.get("exp");
        if (expClaim instanceof Number expNumber) {
            return Optional.of(Instant.ofEpochSecond(expNumber.longValue()));
        }
        return Optional.empty();
    }

    /**
     * Keeps the token out of logs and stack traces.
     *
     * @return a description with the token value redacted
     */
    @Override
    public String toString() {
        return "BearerToken[bearer=<redacted>, expiresAt=" + getExpiresAt().orElse(null) + "]";
    }
}
