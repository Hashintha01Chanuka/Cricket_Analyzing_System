package com.cricket.match.kafka;

import com.cricket.match.event.BallBowledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchEventProducer {

    private final KafkaTemplate<String, BallBowledEvent> kafkaTemplate;

    public void publishBallBowled(BallBowledEvent event) {
        try {
            kafkaTemplate.send(KafkaTopicConfig.BALL_EVENTS_TOPIC, event.matchId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish BallBowledEvent for match {}: {}", event.matchId(), e.getMessage());
        }
    }
}