package com.foodapp.promotion_service.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.promotion_service.api.controller.PromotionChangedEventPayload;
import com.foodapp.promotion_service.domain.model.enums.MaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockPromotionConsumer {

    private final ObjectMapper mapper;

    //Listen to the topic defined in application.yaml
    @KafkaListener(topics="${app.kafka.topic}", groupId = "mock-verifier-group")
    public void consume(ConsumerRecord<String, String> record) {
        String message = record.value();

        try {
            PromotionChangedEventPayload event = mapper.readValue(message, PromotionChangedEventPayload.class);

            routeEvent(event);
        }
        catch (Exception e) {
            log.error("Mock receiver failed to process message: {}", message, e);
        }
    }

    private void routeEvent(PromotionChangedEventPayload event) {
        List<MaskType> mask = event.changeMask();

        boolean isHeavyUpdate = mask.contains(MaskType.DATES)
                || mask.contains(MaskType.SCHEDULE)
                || mask.contains(MaskType.PRIORITY)
                || mask.contains(MaskType.STATUS);

        if (isHeavyUpdate) {
            // ---Scenario A: STRUCTURE CHANGED---
            log.info("ROUTING [EXPANDING SERVICE]");
            log.info("Reason: Structural changes detected: (Dates/Schedule/Priority).");
            log.info("Action: Trigger full time-slice recalculation & collision check.");
        }

        else if(mask.contains(MaskType.EFFECT)) {
            // ---Scenario B: PRICE CHANGED ONLY---
            log.info("ROUTING [Writer SERVICE]");
            log.info("Reason: Only Value/Price changed.");
            log.info("Action: Fast SQL UPDATE to 'discount_value' column.");

            if (event.rules() != null && event.rules().getEffect() != null) {
                log.info("      >> New Value: {}", event.rules().getEffect().getValue());
            }
        }

        else {
            // --- SCENARIO C: METADATA ONLY ---
            log.info("ROUTING: [AUDIT LOG / NO-OP]");
            log.info("Reason: No logic-impacting changes detected.");
        }
    }

}
