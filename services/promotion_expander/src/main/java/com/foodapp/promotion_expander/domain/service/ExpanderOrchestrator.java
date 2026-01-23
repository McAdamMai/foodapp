package com.foodapp.promotion_expander.domain.service;

import com.foodapp.promotion_expander.domain.model.ExpanderEvent;
import com.foodapp.promotion_expander.domain.model.PromotionRules;
import com.foodapp.promotion_expander.domain.model.TimeSlice;
import com.foodapp.promotion_expander.domain.model.TrackerItem;
import com.foodapp.promotion_expander.domain.model.enums.ExpanderAction;
import com.foodapp.promotion_expander.domain.model.enums.MaskType;
import com.foodapp.promotion_expander.domain.model.enums.PromotionStatus;
import com.foodapp.promotion_expander.infra.persistence.entity.ExpanderTrackerEntity;
import com.foodapp.promotion_expander.infra.persistence.entity.TimeSliceEntity;
import com.foodapp.promotion_expander.infra.persistence.repository.ExpanderTrackerRepository;
import com.foodapp.promotion_expander.infra.persistence.repository.TimeSliceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
    @Value("${expander.horizontal-days:30}")
    private int horizontalDays;

    @Value("${app.service.batch-size:1000}")
    private int batchSize;

    @Value("${app.service.day-buffer:1}")
    private int TIMEZONE_BUFFER_DAYS;

    // Any internal calls (like this.executeRebuild) will then inherit and participate in that existing transaction
    @Transactional
    public void processEvent(ExpanderEvent event) {
        // PRE-CALCULATION (CPU Only - Cheap)
        // Date is always local
        // We use UTC Date for the Tracker (as discussed previously)
        LocalDate validStart = event.getStartDateTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate validEnd = event.getEndDateTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDate();

        LocalDate horizonCap = LocalDate.now(ZoneId.of("UTC")).plusDays(horizontalDays);

        LocalDate coveredUntil = null;

        // Determine if we need to expand anything right now (Scenario 1 & 2 Logic)
        // If not, only store it in tracker table
        if (!validStart.isAfter(horizonCap)) {
            coveredUntil = validEnd.isBefore(horizonCap) ? validEnd : horizonCap;
        }

        int isNewer = tracker.updateVersionIfNewer(domainToEntity(
                TrackerItem.builder()
                        .promotionId(event.getPromotionId())
                        .lastProcessedVersion(event.getVersion())
                        .startDate(validStart)
                        .endDate(validEnd)
                        .coverUntil(coveredUntil)
                        .promotionStatus(PromotionStatus.ACTIVE)
                        .rules(event.getRules())
                        .build()
        ));

        // Idempotent
        if (isNewer == 0) {
            log.warn("Stale or Duplicate event received. Promotion: {}, Version: {}. Ignoring.",
                    event.getPromotionId(), event.getVersion());
            return;
        }

        // determine the action
        ExpanderAction action = determineAction(event);

        switch (action){
            case FULL_REBUILD -> executeRebuild(event, validStart, validEnd, coveredUntil);
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
    private void executeRebuild(ExpanderEvent event, LocalDate startUTC, LocalDate endUTC, LocalDate coveredUntil) {
        repo.deleteSlicesByPromotionId(event.getPromotionId());
        // Optimization: If coveredUntil is null (Scenario 1: Future Promo),
        // we skip slicing entirely!
        if (coveredUntil == null) {
            log.info("Promotion starts in future (> 30 days). Rules stored, Slices deferred.");
            return;
        }

        // THE BRIDGE: UTC -> Local Buffer Strategy, ensure no promotion will be missed
        // TBD: using the previous endUTC to compute the range
        LocalDate rangeStart = startUTC.minusDays(TIMEZONE_BUFFER_DAYS);
        LocalDate rangeEnd = coveredUntil.plusDays(TIMEZONE_BUFFER_DAYS);

        Instant clipStart = event.getStartDateTime().toInstant();
        Instant clipEnd = event.getEndDateTime().toInstant();

        List<TimeSlice> timeSlices = engine.expand(event.getRules(), rangeStart, rangeEnd, clipStart, clipEnd);

        // early exit
        // In a rebuild, you must delete old slices first, otherwise you'll have duplicates or stale data.
        if (timeSlices.isEmpty()) {
            return;
        }

        List<TimeSliceEntity> entities = timeSlices.stream()
                .map(this::domainToEntity)
                .toList();

        // Batch insert
        for(int i = 0; i < entities.size(); i+=batchSize) {
            int end = Math.min(entities.size(), i + batchSize);
            List<TimeSliceEntity> batch = entities.subList(i, end);
            repo.insertBatch(batch);
            log.info("Insert {} slices {} - {}", batch.size(), i, end);
        }

        log.info("Successfully replaced {} slices", entities.size());

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

    private ExpanderTrackerEntity domainToEntity(TrackerItem item) {
        return ExpanderTrackerEntity.builder()
                .promotionId(item.getPromotionId())
                .lastProcessedVersion(item.getLastProcessedVersion())
                .updatedAt(Instant.now())
                .validStart(item.getStartDate())
                .validEnd(item.getEndDate())
                .coveredUntil(item.getCoverUntil())
                .status(item.getPromotionStatus())
                .build();
    }
}
