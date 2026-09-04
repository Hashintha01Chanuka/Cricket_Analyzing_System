package com.cricket.stats.client;

import com.cricket.stats.dto.MatchRunRateResponse;
import com.cricket.stats.exception.MatchServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * Synchronous REST call from Stats Service to Match Service. This is
 * deliberately temporary: it's a fine way to prove the analytics logic
 * works end-to-end, but a synchronous call means Stats Service is now
 * coupled to Match Service's uptime — if Match Service is down, this
 * endpoint fails too. Phase 3 replaces this specific call with a Kafka
 * consumer that reacts to ball events as they happen, removing the runtime
 * dependency entirely. Keeping this call isolated in one small class (rather
 * than scattered through StatsService) is what makes that swap a localized
 * change later instead of a rewrite.
 */
@Component
@RequiredArgsConstructor
public class MatchServiceClient {

    private final RestClient matchServiceRestClient;

    public MatchRunRateResponse getCurrentRunRate(UUID matchId, int innings, int oversWindow) {
        try {
            return matchServiceRestClient.get()
                    .uri("/api/v1/matches/{matchId}/run-rate?innings={innings}&overs={overs}",
                            matchId, innings, oversWindow)
                    .retrieve()
                    .body(MatchRunRateResponse.class);
        } catch (Exception e) {
            throw new MatchServiceUnavailableException(
                    "Could not reach Match Service for match " + matchId, e);
        }
    }
}
