package com.foodapp.promotion_expander.domain.service;

import com.foodapp.promotion_expander.domain.model.ExpanderEvent;
import com.foodapp.promotion_expander.domain.model.PromotionRules;
import com.foodapp.promotion_expander.domain.model.TimeSlice;
import com.foodapp.promotion_expander.domain.model.enums.ExpanderAction;
import com.foodapp.promotion_expander.domain.model.enums.MaskType;
import com.foodapp.promotion_expander.infra.persistence.entity.TimeSliceEntity;
import com.foodapp.promotion_expander.infra.persistence.repository.ExpanderTrackerRepository;
import com.foodapp.promotion_expander.infra.persistence.repository.TimeSliceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExpanderOrchestrator {

    // private ExpanderRepostitory repository;
    private final SlicingEngine engine;
    private final TimeSliceRepository repo;
    private final ExpanderTrackerRepository tracker;

    // The Rolling Horizon Strategy
    @Value("${expander.horizontal-days:90}")
    private int horizontalDays;

    // Any internal calls (like this.executeRebuild) will then inherit and participate in that existing transaction
    @Transactional
    public void processEvent(ExpanderEvent event) {

        int isNewer = tracker.updateVersionIfNewer(event.getPromotionId(), event.getVersion());

        if (isNewer == 0) {
            log.warn("Stale or Duplicate event received. Promotion: {}, Version: {}. Ignoring.",
                    event.getPromotionId(), event.getVersion());
            return;
        }

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

    // Transactional is called by Spring, is it is set to private, it can not be called
    // protected is sitting b/w private and public
    private void executeRebuild(ExpanderEvent event) {
        log.info("Rebuilding expander event");
        Instant startDate = event.getStartDateTime().toInstant();
        Instant endDate = event.getEndDateTime().toInstant();
        Instant horizontalCap = Instant.now().plus(horizontalDays, ChronoUnit.DAYS);
        PromotionRules rules = event.getRules();

        List<TimeSlice> timeSlices = engine.expand(rules, startDate, endDate, horizontalCap);

        List<TimeSliceEntity> entities = timeSlices.stream()
                .map(this::domainToEntity)
                .toList();
        if(!entities.isEmpty()){
            repo.deleteSlicesByPromotionId(event.getPromotionId());
            repo.insertBatch(entities);
            log.info("Replace {} slices", entities.size());
        }
    }

    private void executeFastUpdate(ExpanderEvent event) {
        UUID promotionId = event.getPromotionId();
        String type = event.getRules().getEffect().getType();
        double newValue = event.getRules().getEffect().getValue();

        TimeSliceEntity changes = new TimeSliceEntity();
        changes.setEffectType(type);
        changes.setEffectValue(newValue);

        repo.updateContentByPromotionId(promotionId, changes);

        log.info("Fast update rule to type: {} and value: {}", type, newValue);
    }

    private void executeDelete(ExpanderEvent event) {
        UUID promotionId = event.getPromotionId();

        repo.deleteSlicesByPromotionId(promotionId);

        log.info("Deleting expander event: {}", promotionId);
    }

    private TimeSliceEntity domainToEntity(TimeSlice slice) {
        return TimeSliceEntity.builder()
                .id(UUID.randomUUID())
                .promotionId(slice.getPromotionId())
                .version(slice.getVersion())
                .sliceDate(slice.getDate())
                .startTime(slice.getStart())
                .endTime(slice.getEnd())
                .timezone(slice.getPromotionRules().getSchedule().getTimezone())
                .effectType(slice.getPromotionRules().getEffect().getType())
                .effectValue(slice.getPromotionRules().getEffect().getValue())
                .build();
    }
}
