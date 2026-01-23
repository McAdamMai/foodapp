package com.foodapp.promotion_expander.domain.service;

import com.foodapp.promotion_expander.domain.model.PromotionRules;
import com.foodapp.promotion_expander.domain.model.ScheduleRule;
import com.foodapp.promotion_expander.domain.model.EffectRule;
import com.foodapp.promotion_expander.domain.model.TimeSlice;
import com.foodapp.promotion_expander.infra.persistence.entity.ExpanderTrackerEntity;
import com.foodapp.promotion_expander.infra.persistence.entity.TimeSliceEntity;
import com.foodapp.promotion_expander.infra.persistence.repository.ExpanderTrackerRepository;
import com.foodapp.promotion_expander.infra.persistence.repository.TimeSliceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RollingHorizonServiceTest {

    @Mock private ExpanderTrackerRepository tracker;
    @Mock private TimeSliceRepository repo;
    @Mock private SlicingEngine engine;

    @InjectMocks
    private RollingHorizonService service;

    // Constants for testing
    private final int HORIZON_DAYS = 10;
    private final int BATCH_SIZE = 5;
    private final int BUFFER_DAYS = 1;

    @BeforeEach
    void setUp() {
        // Inject @Value fields manually since we are not loading Spring Context
        ReflectionTestUtils.setField(service, "horizonDays", HORIZON_DAYS);
        ReflectionTestUtils.setField(service, "rollingBatchSize", BATCH_SIZE);
        ReflectionTestUtils.setField(service, "TIMEZONE_BUFFER_DAYS", BUFFER_DAYS);
    }

    // =========================================================================
    // 1. NIGHTLY JOB LOOP TESTS
    // =========================================================================

    @Test
    @DisplayName("Job Loop: Should process batches until no candidates remain")
    void executeNightlyRollout_ShouldLoopUntilEmpty() {
        // ARRANGE
        ExpanderTrackerEntity promo1 = createEntity(UUID.randomUUID());
        ExpanderTrackerEntity promo2 = createEntity(UUID.randomUUID());

        // Batch 1: Returns 2 items
        when(tracker.findBatchNeedingExtension(any(), eq(BATCH_SIZE)))
                .thenReturn(List.of(promo1, promo2)) // First call
                .thenReturn(Collections.emptyList()); // Second call (Stop condition)

        // Mock engine to avoid NullPointer on logic
        when(engine.expand(any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());

        // ACT
        service.executeNightlyRollout();

        // ASSERT
        // Should call findBatch twice (Once for data, once to realize it's done)
        verify(tracker, times(2)).findBatchNeedingExtension(any(), eq(BATCH_SIZE));

        // Should update both promotions
        verify(tracker).updateCoveredUntil(eq(promo1.getPromotionId()), any());
        verify(tracker).updateCoveredUntil(eq(promo2.getPromotionId()), any());
    }

    @Test
    @DisplayName("Resiliency: Exception in one promotion should NOT stop the batch")
    void executeNightlyRollout_ShouldContinueOnFailure() {
        // ARRANGE
        ExpanderTrackerEntity goodPromo = createEntity(UUID.randomUUID());
        ExpanderTrackerEntity badPromo = createEntity(UUID.randomUUID());

        when(tracker.findBatchNeedingExtension(any(), anyInt()))
                .thenReturn(List.of(badPromo, goodPromo))
                .thenReturn(Collections.emptyList());

        // Bad Promo throws Exception during processing
        // We mock the tracker update to throw, simulating a DB failure for that specific row
        doThrow(new RuntimeException("DB Connection Fail"))
                .when(tracker).updateCoveredUntil(eq(badPromo.getPromotionId()), any());

        // ACT
        service.executeNightlyRollout();

        // ASSERT
        // Verify Good Promo was still attempted (Logic proceeded past the catch block)
        verify(tracker).updateCoveredUntil(eq(goodPromo.getPromotionId()), any());
    }

    // =========================================================================
    // 2. SINGLE EXTENSION LOGIC TESTS (Buffering, Skipping, Dates)
    // =========================================================================

    @Test
    @DisplayName("Buffering: Should apply Buffer Days to Engine Range")
    void processSingleExtension_ShouldApplyBuffer() {
        // ARRANGE
        LocalDate today = LocalDate.now(ZoneId.of("UTC"));
        LocalDate targetHorizon = today.plusDays(HORIZON_DAYS);

        // Setup: Covered until Yesterday. Needs expansion starting Today.
        LocalDate validStart = today.minusDays(5);
        LocalDate validEnd = today.plusDays(20);
        LocalDate coveredUntil = today.minusDays(1);

        ExpanderTrackerEntity entity = createEntity(UUID.randomUUID());
        entity.setValidStart(validStart);
        entity.setValidEnd(validEnd);
        entity.setCoveredUntil(coveredUntil);

        // Mock Engine
        when(engine.expand(any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());

        // ACT
        service.processSingleExtension(entity, targetHorizon);

        // ASSERT
        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);

        verify(engine).expand(any(), startCaptor.capture(), endCaptor.capture(), any(), any());

        // Logic check:
        // Range Start = CoveredUntil(Yesterday) + 1 = Today
        // Buffered Start = Today - 1 Day Buffer = Yesterday
        assertThat(startCaptor.getValue()).isEqualTo(today.minusDays(1));

        // Range End = Horizon
        // Buffered End = Horizon + 1 Day Buffer
        assertThat(endCaptor.getValue()).isEqualTo(targetHorizon.plusDays(1));
    }

    @Test
    @DisplayName("Skip Future: Should NOT call engine if Promotion Start > Horizon")
    void processSingleExtension_ShouldSkipFuturePromotions() {
        // ARRANGE
        LocalDate targetHorizon = LocalDate.now().plusDays(10);

        // Promotion starts in 20 days (Way past horizon)
        ExpanderTrackerEntity futureEntity = createEntity(UUID.randomUUID());
        futureEntity.setValidStart(LocalDate.now().plusDays(20));
        futureEntity.setValidEnd(LocalDate.now().plusDays(30));

        // ACT
        service.processSingleExtension(futureEntity, targetHorizon);

        // ASSERT
        verifyNoInteractions(engine);
        verifyNoInteractions(repo);
        verify(tracker, never()).updateCoveredUntil(any(), any());
    }

    @Test
    @DisplayName("Skip Finished: Should NOT call engine if CoveredUntil >= ValidEnd")
    void processSingleExtension_ShouldSkipFinishedPromotions() {
        // ARRANGE
        LocalDate targetHorizon = LocalDate.now().plusDays(10);

        // Promotion ended yesterday and is fully covered
        ExpanderTrackerEntity finishedEntity = createEntity(UUID.randomUUID());
        finishedEntity.setValidStart(LocalDate.now().minusDays(10));
        finishedEntity.setValidEnd(LocalDate.now().minusDays(1));
        finishedEntity.setCoveredUntil(LocalDate.now().minusDays(1));

        // ACT
        service.processSingleExtension(finishedEntity, targetHorizon);

        // ASSERT
        verifyNoInteractions(engine); // Key Check: Do not expand if already done
        verify(tracker, never()).updateCoveredUntil(any(), any());
    }

    @Test
    @DisplayName("Persistence: Should Map and Save Slices if Engine returns data")
    void processSingleExtension_ShouldSaveSlices() {
        // ARRANGE
        ExpanderTrackerEntity entity = createEntity(UUID.randomUUID());

        // FIX: Use Builder instead of Setters
        TimeSlice mockSlice = TimeSlice.builder()
                .promotionId(entity.getPromotionId())
                .version(1)
                .date(LocalDate.now())
                .start(Instant.now())
                .end(Instant.now())
                .promotionRules(entity.getRules())
                .build();

        when(engine.expand(any(), any(), any(), any(), any())).thenReturn(List.of(mockSlice));

        // ACT
        service.processSingleExtension(entity, LocalDate.now().plusDays(10));

        // ASSERT
        ArgumentCaptor<List<TimeSliceEntity>> batchCaptor = ArgumentCaptor.forClass(List.class);
        verify(repo).insertBatch(batchCaptor.capture());

        List<TimeSliceEntity> savedEntities = batchCaptor.getValue();
        assertThat(savedEntities).hasSize(1);
        assertThat(savedEntities.get(0).getPromotionId()).isEqualTo(entity.getPromotionId());

        verify(tracker).updateCoveredUntil(eq(entity.getPromotionId()), any());
    }

    // --- Helpers ---

    private ExpanderTrackerEntity createEntity(UUID id) {
        PromotionRules rules = new PromotionRules();
        ScheduleRule schedule = new ScheduleRule();
        schedule.setTimezone("UTC");
        EffectRule effect = new EffectRule();
        effect.setType("DISCOUNT");
        effect.setValue(10.0);
        rules.setSchedule(schedule);
        rules.setEffect(effect);

        return ExpanderTrackerEntity.builder()
                .promotionId(id)
                .validStart(LocalDate.now().minusDays(1))
                .validEnd(LocalDate.now().plusDays(30))
                .coveredUntil(null) // Not started yet
                .rules(rules)
                .build();
    }
}