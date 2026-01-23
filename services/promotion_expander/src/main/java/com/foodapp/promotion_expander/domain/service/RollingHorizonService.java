package com.foodapp.promotion_expander.domain.service;

import com.foodapp.promotion_expander.domain.model.TimeSlice;
import com.foodapp.promotion_expander.infra.persistence.entity.ExpanderTrackerEntity;
import com.foodapp.promotion_expander.infra.persistence.entity.TimeSliceEntity;
import com.foodapp.promotion_expander.infra.persistence.repository.ExpanderTrackerRepository;
import com.foodapp.promotion_expander.infra.persistence.repository.TimeSliceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RollingHorizonService {
    private final ExpanderTrackerRepository tracker;
    private final TimeSliceRepository repo;
    private final SlicingEngine engine;

    @Value("${expander.horizontal-days:30}")
    private int horizonDays;

    @Value("${expander.rolling.batch-size:50}")
    private int rollingBatchSize;

    @Value("${app.service.day-buffer:1}")
    private int TIMEZONE_BUFFER_DAYS;

    @Scheduled(cron = "0 0 2 * * ?")
    public void executeNightlyRollout() {
        log.info("Starting Nightly Rolling Horizon Job");

        LocalDate todayUTC = LocalDate.now(ZoneId.of("UTC"));
        LocalDate targetHorizon = todayUTC.plusDays(horizonDays);

        long totalProcessed = 0;
        boolean hasMore = true;

        // Loop until we have processed all promotions needing extension
        while (hasMore) {
            // 1. Fetch a Batch of Candidates
            // We find ACTIVE promotions where covered_until < targetHorizon
            List<ExpanderTrackerEntity> candidates = tracker.findBatchNeedingExtension(targetHorizon, rollingBatchSize);

            if (candidates.isEmpty()) {
                hasMore = false;
                break;
            }

            for (ExpanderTrackerEntity entity : candidates) {
                try {
                    processSingleExtension(entity, targetHorizon);
                } catch (Exception e) {
                    log.error("Failed to extend promotion {}. Skipping.", entity.getPromotionId(), e);
                }
            }

            totalProcessed += candidates.size();
            log.info("Rolling Job: Processed {} promotions so far...", totalProcessed);
        }
        log.info("Nightly Rolling Job Completed. Total Processed: {}", totalProcessed);
    }

    @Transactional
    public void processSingleExtension(ExpanderTrackerEntity entity, LocalDate targetHorizon) {
        if (entity.getValidStart().isAfter(targetHorizon)) {
            log.debug("Skipping future promotion {}. Start {} > Horizon {}",
                    entity.getPromotionId(), entity.getValidStart(), targetHorizon);
            return;
        }
        // Determine the window
        LocalDate rangeStart = (entity.getCoveredUntil() == null ? entity.getValidStart() : entity.getCoveredUntil().plusDays(1));

        LocalDate rangeEnd = (targetHorizon.isBefore(entity.getValidEnd())? targetHorizon : entity.getValidEnd());

        // Logic: We ensure the DB is perfectly synced to the End Date before exiting.
        // When cap is not null but targetHorizon == getValidEnd, it means end
        if (rangeStart.isAfter(rangeEnd)) {
            return;
        }

        // set the buffer
        LocalDate rangeStartBuffered = rangeStart.minusDays(TIMEZONE_BUFFER_DAYS);
        LocalDate rangeEndBuffered = rangeEnd.plusDays(TIMEZONE_BUFFER_DAYS);

        // compute the clip start and end

        // Include the end day
        Instant clipStart = entity.getValidStart().atStartOfDay(ZoneId.of("UTC")).toInstant();
        Instant clipEnd = entity.getValidEnd().plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant();

        List<TimeSlice> slices = engine.expand(
                entity.getRules(),
                rangeStartBuffered,
                rangeEndBuffered,
                clipStart,
                clipEnd
        );

        if(!slices.isEmpty()) {
            List<TimeSliceEntity> entities = slices.stream().map(this::domainToEntity).toList();
            repo.insertBatch(entities);
        }

        tracker.updateCoveredUntil(entity.getPromotionId(), rangeEnd);
    }

    private TimeSliceEntity domainToEntity(TimeSlice slice) {
        return TimeSliceEntity.builder()
                //.id(UUID.randomUUID())
                .promotionId(slice.getPromotionId())
                // Use the tracker version or rules version
                .version(slice.getVersion()) // Simplify for batch, or fetch from rules
                .sliceDate(slice.getDate())
                .startTime(slice.getStart())
                .endTime(slice.getEnd())
                .timezone(slice.getPromotionRules().getSchedule().getTimezone())
                .effectType(slice.getPromotionRules().getEffect().getType())
                .effectValue(slice.getPromotionRules().getEffect().getValue())
                .build();
    }

}
