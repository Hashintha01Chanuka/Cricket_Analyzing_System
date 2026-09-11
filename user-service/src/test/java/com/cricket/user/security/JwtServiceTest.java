package com.cricket.user.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // 32+ char secret, matching the HMAC-SHA key size requirement.
        jwtService = new JwtService("test-secret-key-for-unit-tests-only-32chars", 3600000L);
    }

    @Test
    void generateToken_embedsUsernameAndRole() {
        String token = jwtService.generateToken("kohli", "VIEWER");

        assertThat(jwtService.extractUsername(token)).isEqualTo("kohli");
        assertThat(jwtService.extractRole(token)).isEqualTo("VIEWER");
    }

    @Test
    void isTokenValid_trueForMatchingUsernameAndUnexpiredToken() {
        String token = jwtService.generateToken("kohli", "SCORER");

        assertThat(jwtService.isTokenValid(token, "kohli")).isTrue();
    }

    @Test
    void isTokenValid_falseForMismatchedUsername() {
        String token = jwtService.generateToken("kohli", "SCORER");

        assertThat(jwtService.isTokenValid(token, "someone-else")).isFalse();
    }

    @Test
    void isTokenValid_falseForExpiredToken() throws InterruptedException {
        JwtService shortLived = new JwtService("test-secret-key-for-unit-tests-only-32chars", 1L);
        String token = shortLived.generateToken("kohli", "VIEWER");

        Thread.sleep(10); // let it expire

        assertThat(shortLived.isTokenValid(token, "kohli")).isFalse();
    }

    @Test
    void extractExpiration_isInTheFuture() {
        String token = jwtService.generateToken("kohli", "VIEWER");

        assertThat(jwtService.extractExpiration(token)).isAfter(Instant.now());
    }
}
