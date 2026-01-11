package com.foodapp.promotion_expander.infra.messaging;

import com.foodapp.promotion_expander.domain.model.ExpanderEvent;
import com.foodapp.promotion_expander.domain.service.ExpanderOrchestrator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        // CRITICAL FIX: Override your YAML config to use the random Embedded Kafka port
        properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
)
@DirtiesContext // Ensures a clean Context/Kafka for this test
@EmbeddedKafka(
        partitions = 1,
        topics = { "${app.kafka.topics.promotion-updates}" }
)
class PromotionEventListenerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // We mock the Orchestrator to verify the Listener passed the correct object
    @MockBean
    private ExpanderOrchestrator orchestrator;

    @Value("${app.kafka.topics.promotion-updates}")
    private String topic;

    @Test
    @DisplayName("Should deserialize Raw Producer JSON into ExpanderEvent via @JsonAlias")
    void testConsumePromotionEvent() throws Exception {
        // ==========================================
        // 1. PREPARE: Create the Raw JSON (Simulating Producer)
        // ==========================================
        UUID messageId = UUID.randomUUID();
        UUID promotionId = UUID.randomUUID();
        String startDateStr = "2024-01-01T10:00:00Z"; // ISO-8601

        // This JSON matches PromotionChangedEventPayload (Producer's structure)
        // Notice fields like "status", "startDate" which differ from ExpanderEvent names
        String producerPayloadJson = """
            {
                "messageId": "%s",
                "promotionId": "%s",
                "promotionVersion": 1,
                "status": "PUBLISHED",
                "changeMask": ["SCHEDULE", "EFFECT"],
                "startDate": "%s",
                "endDate": "2024-01-31T23:59:59Z",
                "templateId": "%s",
                "rules": {} 
            }
        """.formatted(messageId, promotionId, startDateStr, UUID.randomUUID());

        // ==========================================
        // 2. EXECUTE: Send to Embedded Kafka
        // ==========================================
        kafkaTemplate.send(topic, promotionId.toString(), producerPayloadJson);

        // ==========================================
        // 3. VERIFY: Capture what the Listener received
        // ==========================================
        ArgumentCaptor<ExpanderEvent> eventCaptor = ArgumentCaptor.forClass(ExpanderEvent.class);

        // Wait up to 5 seconds for the Async Listener to process the message
        verify(orchestrator, timeout(500).times(1))
                .processEvent(eventCaptor.capture());

        ExpanderEvent receivedEvent = eventCaptor.getValue();

        // ==========================================
        // 4. ASSERTIONS: Did @JsonAlias work?
        // ==========================================
        assertThat(receivedEvent).isNotNull();

        // Identity checks
        assertThat(receivedEvent.getMessageId()).isEqualTo(messageId);
        assertThat(receivedEvent.getPromotionId()).isEqualTo(promotionId);

        // @JsonAlias("promotionVersion") -> version
        assertThat(receivedEvent.getVersion()).isEqualTo(1);

        // @JsonAlias("status") -> promotionStatus
        assertThat(receivedEvent.getPromotionStatus()).isEqualTo("PUBLISHED");

        // @JsonAlias("startDate") -> startDateTime (OffsetDateTime parsing)
        assertThat(receivedEvent.getStartDateTime())
                .isEqualTo(OffsetDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC));

        System.out.println("Test Passed: JSON correctly mapped to ExpanderEvent!");
    }
}