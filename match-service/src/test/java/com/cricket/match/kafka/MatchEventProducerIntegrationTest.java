package com.cricket.match.kafka;

import com.cricket.match.event.BallBowledEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spins up a real in-memory Kafka broker (no Docker needed for this test)
 * and proves MatchEventProducer actually publishes a readable message to
 * "ball-events" — as opposed to just trusting that KafkaTemplate.send()
 * compiles. Deliberately kept separate from MatchControllerIntegrationTest
 * (which uses a real Postgres via Testcontainers) so each test only pays
 * for the infrastructure it actually needs.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:matchservice-kafka-test;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@EmbeddedKafka(partitions = 1, topics = {KafkaTopicConfig.BALL_EVENTS_TOPIC})
class MatchEventProducerIntegrationTest {

    @Autowired
    private MatchEventProducer matchEventProducer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @DynamicPropertySource
    static void kafkaBootstrapServers(DynamicPropertyRegistry registry) {
        // EmbeddedKafka starts on a random port; point the app's producer
        // config at it instead of the real localhost:9092 default.
        registry.add("spring.kafka.bootstrap-servers",
                () -> System.getProperty("spring.embedded.kafka.brokers"));
    }

    @Test
    void publishBallBowled_sendsReadableMessageToTopic() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                embeddedKafkaBroker, "test-group", true);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, BallBowledEvent.class.getName());

        Consumer<String, BallBowledEvent> consumer = new KafkaConsumer<>(
                consumerProps,
                new org.apache.kafka.common.serialization.StringDeserializer(),
                new JacksonJsonDeserializer<>(BallBowledEvent.class, false));
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, KafkaTopicConfig.BALL_EVENTS_TOPIC);

        UUID matchId = UUID.randomUUID();
        BallBowledEvent event = new BallBowledEvent(
                UUID.randomUUID(), matchId, 1, 5, 3,
                UUID.randomUUID(), "Kohli", 4, 22, 26,
                UUID.randomUUID(), "Bumrah", false, 0,
                false, false, 0, Instant.now());

        matchEventProducer.publishBallBowled(event);

        ConsumerRecord<String, BallBowledEvent> record =
                KafkaTestUtils.getSingleRecord(
                        consumer,
                        KafkaTopicConfig.BALL_EVENTS_TOPIC,
                        Duration.ofSeconds(10)
                );

        assertThat(record.key()).isEqualTo(matchId.toString());
        assertThat(record.value().batsmanNewRuns()).isEqualTo(26);
        assertThat(record.value().batsmanName()).isEqualTo("Kohli");

        consumer.close();
    }
}
