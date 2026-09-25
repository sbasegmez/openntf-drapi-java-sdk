/*
 * Copyright (c) 2026 Serdar Basegmez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.drapi.internal.dto;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.openntf.drapi.auth.Token;
import org.openntf.drapi.util.TypeUtils;

public record AuthResponse(String bearer, Map<String, Object> claims, int leeway, int expSeconds, String issueDate) {

    public AuthResponse {
        claims = TypeUtils.defaultIfEmpty(claims, Map.of());
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
        return expiresAt().map(expiry -> Instant.now().isAfter(expiry.minusSeconds(skew)))
                             .orElse(false);
    }

    /**
     * Returns the subject of the token, if present.
     *
     * @return the subject of the token, if present
     */
    public Optional<String> subject() {
        Object subClaim = claims.get("sub");
        if (subClaim instanceof String sub) {
            return Optional.of(sub);
        }
        return Optional.empty();
    }

    /**
     * Converts this AuthResponse to a Token object.
     *
     * @return a Token object containing the bearer token and subject
     */
    public Token toToken() {
        return new Token(bearer, subject().orElse("[no-subject]"));
    }

    /**
     * Returns the expiry time of the token, if known.
     *
     * @return the expiry time of the token, if known
     */
    private Optional<Instant> expiresAt() {
        Object expClaim = claims.get("exp");
        if (expClaim instanceof Number expNumber) {
            return Optional.of(Instant.ofEpochSecond(expNumber.longValue()));
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
            "bearer='" + "********" + '\'' +
            ", claims=" + claims +
            ", leeway=" + leeway +
            ", expSeconds=" + expSeconds +
            ", issueDate='" + issueDate + '\'' +
            '}';
    }
}
