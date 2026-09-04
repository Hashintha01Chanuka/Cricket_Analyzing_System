package com.cricket.match.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String BALL_EVENTS_TOPIC = "ball-events";

    @Bean
    public NewTopic ballEventsTopic() {
        return TopicBuilder.name(BALL_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}