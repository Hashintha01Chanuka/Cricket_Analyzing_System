package com.cricket.notification.kafka;

import com.cricket.notification.event.BallBowledEvent;
import com.cricket.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Proves the whole pipeline end-to-end: publish a BallBowledEvent that
 * crosses the fifty threshold onto an in-memory Kafka broker, and assert a
 * FIFTY notification actually lands in the (H2, in-memory) database. This
 * is the strongest possible evidence the Kafka wiring works, short of
 * running it against a real broker.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:notificationservice-kafka-test;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@EmbeddedKafka(partitions = 1, topics = {"ball-events"})
class BallEventConsumerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private NotificationRepository notificationRepository;

    @DynamicPropertySource
    static void kafkaBootstrapServers(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers",
                () -> System.getProperty("spring.embedded.kafka.brokers"));
    }

    @Test
    void ballEventCrossingFifty_resultsInPersistedNotification() {
        KafkaTemplate<String, BallBowledEvent> producer = testProducer();

        UUID matchId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        BallBowledEvent event = new BallBowledEvent(
                UUID.randomUUID(), matchId, 1, 10, 2,
                playerId, "Rohit Sharma", 4, 48, 52,
                UUID.randomUUID(), "Starc", false, 0,
                false, false, 0, Instant.now());

        producer.send("ball-events", matchId.toString(), event);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(notificationRepository.findByPlayerIdOrderByCreatedAtDesc(playerId)).hasSize(1));

        var notification = notificationRepository.findByPlayerIdOrderByCreatedAtDesc(playerId).get(0);
        assertThat(notification.getMessage()).contains("Rohit Sharma");
        assertThat(notification.getType().name()).isEqualTo("FIFTY");
    }

    private KafkaTemplate<String, BallBowledEvent> testProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        ProducerFactory<String, BallBowledEvent> pf = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(pf);
    }
}
