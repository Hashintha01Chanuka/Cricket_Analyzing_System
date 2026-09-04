package com.cricket.stats.exception;

import java.util.UUID;

public class LiveStateNotAvailableException extends RuntimeException {

    public LiveStateNotAvailableException(UUID matchId, int innings) {
        super("No live state yet for match " + matchId + ", innings " + innings +
                " — either no balls have been bowled, or Kafka hasn't caught up yet");
    }
}