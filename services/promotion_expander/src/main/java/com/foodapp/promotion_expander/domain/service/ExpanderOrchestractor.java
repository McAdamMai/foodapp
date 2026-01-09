package com.foodapp.promotion_expander.domain.service;

import com.foodapp.promotion_expander.domain.model.EffectRule;
import com.foodapp.promotion_expander.domain.model.ExpanderEvent;
import com.foodapp.promotion_expander.domain.model.PromotionRules;
import com.foodapp.promotion_expander.domain.model.TimeSlice;
import com.foodapp.promotion_expander.domain.model.enums.ExpanderAction;
import com.foodapp.promotion_expander.domain.model.enums.MaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExpanderOrchestractor {

    // private ExpanderRepostitory repository;
    private final SlicingEngine engine;

    // The Rolling Horizon Strategy
    @Value("${expander.horizontal-days:90}")
    private int horizontalDays;

    public void processEvent(ExpanderEvent event) {

        // determine the action
        ExpanderAction action = determineAction(event);

        switch (action){
            case FULL_REBUILD -> executeRebuild(event);
            case FAST_UPDATE -> executeFastUpdate(event);
            case DELETE ->  executeDelete(event);
            case NO_OP -> log.info("Ignoring event: {}", event.getMessageId());
        }

    }

    private ExpanderAction determineAction(ExpanderEvent event) {

        List<MaskType> mask = event.getChangeMask();

        // Safety: if the status is not set PUBLISH, we usually delete;
        // TBD Promotion get ROLLBACK;
        if (! "PUBLISHED".equals(event.getPromotionStatus())) {
            return ExpanderAction.DELETE;
        }
        // Heavy update
        if (mask.contains(MaskType.SCHEDULE) || mask.contains(MaskType.DATES) || mask.contains(MaskType.PRIORITY)) {
            return ExpanderAction.FULL_REBUILD;
        }

        // Light update
        if (mask.contains(MaskType.EFFECT)) {
            return ExpanderAction.FAST_UPDATE;
        }

        // Only Meta
        if (mask.contains(MaskType.META)) {
            return ExpanderAction.NO_OP;
        }
        // default fallback, but why?
        return ExpanderAction.FULL_REBUILD;
    }

    // Instant are used to do comparison
    private void executeRebuild(ExpanderEvent event) {
        log.info("Rebuilding expander event");
        Instant startDate = event.getStartDateTime().toInstant();
        Instant endDate = event.getEndDateTime().toInstant();
        Instant horizontalCap = Instant.now().plus(horizontalDays, ChronoUnit.DAYS);
        PromotionRules rules = event.getRules();

        List<TimeSlice> timeSlices = engine.expand(rules, startDate, endDate, horizontalCap);

        timeSlices.forEach(s -> {
            s.builder()
                    .promotionId(event.getPromotionId())
                    .version(event.getVersion())
                    .build();
        });
        // TBD Persistent
        // repository.replaceSlicesForPromotion();
    }

    private void executeFastUpdate(ExpanderEvent event) {
        // assume time slots are already correct in the DB

        // extract the value
        double newValue = event.getRules().getEffect().getValue();
        String type = event.getRules().getEffect().getType();

        // TBD persistence
        // repository.updateSliceProperties();
        log.info("Fast update rule to type: {} and value: {}", type, newValue);
    }

    private void executeDelete(ExpanderEvent event) {
        UUID promotionId = event.getPromotionId();
        // TBD persistence
        // repository.deleteByPromotionId();
        log.info("Deleting expander event: {}", promotionId);
    }
}
