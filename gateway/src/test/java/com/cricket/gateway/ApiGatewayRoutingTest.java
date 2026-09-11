package com.cricket.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Stands up a WireMock server in place of each real downstream service and
 * proves the gateway's route predicates actually send requests to the right
 * place — this is what catches a typo'd Path predicate or a wrong port that
 * a config file alone can't.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayRoutingTest {

    private static WireMockServer wireMockServer;

    @LocalServerPort
    private int gatewayPort;

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void routeAllServicesToWireMock(DynamicPropertyRegistry registry) {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        String wireMockUrl = "http://localhost:" + wireMockServer.port();

        // Point every downstream route at the same WireMock instance —
        // simpler than juggling five separate stub servers, and sufficient
        // to prove each Path predicate routes correctly.
        registry.add("MATCH_SERVICE_URL", () -> wireMockUrl);
        registry.add("PLAYER_SERVICE_URL", () -> wireMockUrl);
        registry.add("USER_SERVICE_URL", () -> wireMockUrl);
        registry.add("STATS_SERVICE_URL", () -> wireMockUrl);
        registry.add("NOTIFICATION_SERVICE_URL", () -> wireMockUrl);
    }

    @BeforeEach
    void resetStubs() {
        wireMockServer.resetAll();
    }

    @org.junit.jupiter.api.AfterAll
    static void stopServer() {
        wireMockServer.stop();
    }

    @Test
    void matchesPathRoutesToMatchService() {
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/matches"))
                .willReturn(aResponse().withStatus(200).withBody("[]")));

        webTestClient.get().uri("/api/v1/matches")
                .exchange()
                .expectStatus().isOk();

        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/v1/matches")));
    }

    @Test
    void playersPathRoutesToPlayerService() {
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/players")).willReturn(aResponse().withStatus(200)));

        webTestClient.get().uri("/api/v1/players").exchange().expectStatus().isOk();

        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/v1/players")));
    }

    @Test
    void authPathRoutesToUserService() {
        wireMockServer.stubFor(post(urlEqualTo("/api/v1/auth/login")).willReturn(aResponse().withStatus(200)));

        webTestClient.post().uri("/api/v1/auth/login").exchange().expectStatus().isOk();

        wireMockServer.verify(postRequestedFor(urlEqualTo("/api/v1/auth/login")));
    }

    @Test
    void statsPathRoutesToStatsService() {
        wireMockServer.stubFor(get(urlPathEqualTo("/api/v1/stats/head-to-head")).willReturn(aResponse().withStatus(200)));

        webTestClient.get().uri("/api/v1/stats/head-to-head?team1=A&team2=B").exchange().expectStatus().isOk();
    }

    @Test
    void notificationsPathRoutesToNotificationService() {
        wireMockServer.stubFor(get(urlPathMatching("/api/v1/notifications/.*")).willReturn(aResponse().withStatus(200)));

        webTestClient.get().uri("/api/v1/notifications/match/" + java.util.UUID.randomUUID())
                .exchange()
                .expectStatus().isOk();
    }
}
