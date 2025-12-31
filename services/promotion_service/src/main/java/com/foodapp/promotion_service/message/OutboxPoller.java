package com.foodapp.promotion_service.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.promotion_service.persistence.entity.PromotionOutboxEntity;
import com.foodapp.promotion_service.persistence.repository.PromotionOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final PromotionOutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // Config: Topic Name
    private static final String TOPIC = "promotion-updates";

    @Scheduled(fixedDelay = 2000) // Run every 2 seconds
    @Transactional
    public void pollAndPublish() {
        // 1. Fetch pending messages
        List<PromotionOutboxEntity> pendingEvents = repository.findPendingBatch(10);

        if (pendingEvents.isEmpty()) return;

        log.debug("Found {} pending outbox events.", pendingEvents.size());

        for (PromotionOutboxEntity event : pendingEvents) {
            try {
                // 2. Send to Kafka
                // KEY is critical: Use aggregateId (PromotionID) to ensure ordering per promotion
                String key = event.getAggregateId().toString();
                String payload = event.getPayload(); // This is the JSON string we stored

                kafkaTemplate.send(TOPIC, key, payload).get();
                repository.markAsPublished(event.getId(), OffsetDateTime.now());
                log.debug("Published outbox event {}", event.getId());

            } catch (Exception e) {
                log.error("Error processing outbox event {}", event.getId(), e);
            }
        }
    }
}