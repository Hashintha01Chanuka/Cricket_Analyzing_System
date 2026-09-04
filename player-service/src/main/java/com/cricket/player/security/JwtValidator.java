package com.cricket.player.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Optional;

/**
 * Same design as Match Service's JwtValidator: verifies tokens issued by
 * User Service using the shared signing secret, no DB lookup, no call back
 * to User Service. See that class's javadoc for the revocation tradeoff
 * this implies.
 */
@Component
public class JwtValidator {

    private final SecretKey signingKey;

    public JwtValidator(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Optional<AuthenticatedUser> validate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            return Optional.of(new AuthenticatedUser(username, role));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public record AuthenticatedUser(String username, String role) {
    }
}