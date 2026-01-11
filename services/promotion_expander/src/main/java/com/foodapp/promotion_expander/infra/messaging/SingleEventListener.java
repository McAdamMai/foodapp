package com.foodapp.promotion_expander.infra.messaging;

import com.foodapp.promotion_expander.domain.model.ExpanderEvent;
import com.foodapp.promotion_expander.domain.service.ExpanderOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SingleEventListener {

    private final ExpanderOrchestrator orchestrator;

    /**
     * Listens to the promotion-updates topic.
     * The 'topics' value usually comes from application.properties for flexibility.
     */

    @KafkaListener(
            topics = "${app.kafka.topics.promotion-updates}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPromotionEvent(ExpanderEvent event) {
        log.info("Received event {} on promotion {}",
                event.getMessageId(),
                event.getPromotionId());

        try {
            // 1. The 'event' object is already populated with correct data
            // due to the @JsonAlias annotations in ExpanderEvent.
            orchestrator.processEvent(event);

            log.info("Successfully processed event {}", event.getMessageId());

        } catch (Exception e) {
            log.error("Failed to process event {}", event.getMessageId(), e);
            // Re-throw to trigger Kafka retry/DLQ policies
            throw e;
        }
    }
}
