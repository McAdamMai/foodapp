package com.foodapp.promotion_expander.infra.persistence.repository;

import com.foodapp.promotion_expander.domain.model.EffectRule;
import com.foodapp.promotion_expander.domain.model.PromotionRules;
import com.foodapp.promotion_expander.domain.model.ScheduleRule;
import com.foodapp.promotion_expander.domain.model.enums.PromotionStatus;
import com.foodapp.promotion_expander.infra.persistence.entity.ExpanderTrackerEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test") // Ensures H2 or test-specific config is loaded
class ExpanderTrackerRepositoryTest {

    @Autowired
    private ExpanderTrackerRepository trackerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =========================================================================
    // 1. OPTIMISTIC LOCKING TEST (Gatekeeper)
    // =========================================================================
    @Test
    @DisplayName("Gatekeeper: Should allow new versions, reject old ones (Optimistic Locking)")
    void testOptimisticLockingLifecycle() {
        UUID promotionId = UUID.randomUUID();

        // SCENARIO 1: First Event (Version 1) - Should Insert
        ExpanderTrackerEntity v1 = createDummyEntity(promotionId, 1, PromotionStatus.ACTIVE);
        int rowsUpdated = trackerRepository.updateVersionIfNewer(v1);

        assertThat(rowsUpdated).isEqualTo(1);
        assertDbVersion(promotionId, 1);

        // SCENARIO 2: Valid Update (Version 5) - Should Update
        ExpanderTrackerEntity v5 = createDummyEntity(promotionId, 5, PromotionStatus.ACTIVE);
        rowsUpdated = trackerRepository.updateVersionIfNewer(v5);

        assertThat(rowsUpdated).isEqualTo(1);
        assertDbVersion(promotionId, 5);

        // SCENARIO 3: Stale Event (Version 3) - Should Ignore
        ExpanderTrackerEntity v3 = createDummyEntity(promotionId, 3, PromotionStatus.ACTIVE);
        rowsUpdated = trackerRepository.updateVersionIfNewer(v3);

        assertThat(rowsUpdated).isEqualTo(0);
        assertDbVersion(promotionId, 5); // Remains 5
    }

    // =========================================================================
    // 2. CURSOR UPDATE TEST
    // =========================================================================
    @Test
    @DisplayName("UpdateCoveredUntil: Should update only the cursor date")
    void testUpdateCoveredUntil() {
        // ARRANGE
        UUID promotionId = UUID.randomUUID();
        ExpanderTrackerEntity entity = createDummyEntity(promotionId, 1, PromotionStatus.ACTIVE);
        trackerRepository.updateVersionIfNewer(entity); // Insert initial

        LocalDate newDate = LocalDate.now().plusDays(5);

        // ACT
        trackerRepository.updateCoveredUntil(promotionId, newDate);

        // ASSERT
        LocalDate dbDate = jdbcTemplate.queryForObject(
                "SELECT covered_until_date FROM expander_tracker WHERE promotion_id = ?",
                LocalDate.class,
                promotionId
        );
        assertThat(dbDate).isEqualTo(newDate);
    }

    // =========================================================================
    // 3. ROLLING WINDOW QUERY TEST
    // =========================================================================
    @Test
    @DisplayName("FindBatch: Should find ACTIVE items where CoveredUntil < Horizon")
    void testFindBatchNeedingExtension() {
        // ARRANGE
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(10);

        // 1. Candidate: ACTIVE, CoveredUntil = Yesterday (Needs update)
        ExpanderTrackerEntity candidate = createDummyEntity(UUID.randomUUID(), 1, PromotionStatus.ACTIVE);
        candidate.setCoveredUntil(today.minusDays(1));
        trackerRepository.updateVersionIfNewer(candidate);

        // 2. Ignore: ACTIVE, CoveredUntil = Horizon (Already done)
        ExpanderTrackerEntity covered = createDummyEntity(UUID.randomUUID(), 1, PromotionStatus.ACTIVE);
        covered.setCoveredUntil(horizon);
        trackerRepository.updateVersionIfNewer(covered);

        // 3. Ignore: PAUSED, CoveredUntil = Yesterday (Not active)
        ExpanderTrackerEntity paused = createDummyEntity(UUID.randomUUID(), 1, PromotionStatus.PAUSED); // Or PAUSED
        paused.setCoveredUntil(today.minusDays(1));
        trackerRepository.updateVersionIfNewer(paused);

        // 4. Candidate: ACTIVE, CoveredUntil = NULL (New promotion)
        ExpanderTrackerEntity newPromo = createDummyEntity(UUID.randomUUID(), 1, PromotionStatus.ACTIVE);
        newPromo.setCoveredUntil(null);
        trackerRepository.updateVersionIfNewer(newPromo);

        // ACT
        List<ExpanderTrackerEntity> results = trackerRepository.findBatchNeedingExtension(horizon, 100);

        // ASSERT
        // Should find "Candidate" and "NewPromo"
        assertThat(results).hasSize(2);

        // Verify we got the right IDs
        List<UUID> foundIds = results.stream().map(ExpanderTrackerEntity::getPromotionId).toList();
        assertThat(foundIds).containsExactlyInAnyOrder(candidate.getPromotionId(), newPromo.getPromotionId());
    }

    // --- Helpers ---

    private ExpanderTrackerEntity createDummyEntity(UUID id, int version, PromotionStatus status) {
        // Use Nested Builders for Rules
        // 1. Create Schedule using Setters
        ScheduleRule schedule = new ScheduleRule();
        schedule.setTimezone("UTC");

        // 2. Create Effect using Setters
        EffectRule effect = new EffectRule();
        effect.setType("DISCOUNT");
        effect.setValue(10.0);

        // 3. Create Rules using Setters
        PromotionRules rules = new PromotionRules();
        rules.setSchedule(schedule);
        rules.setEffect(effect);

        return ExpanderTrackerEntity.builder()
                .promotionId(id)
                .lastProcessedVersion(version)
                .validStart(LocalDate.now())
                .validEnd(LocalDate.now().plusDays(20))
                .coveredUntil(null)
                .status(status)
                .rules(rules)
                .build();
    }

    private void assertDbVersion(UUID promotionId, int expectedVersion) {
        Integer actualVersion = jdbcTemplate.queryForObject(
                "SELECT last_processed_version FROM expander_tracker WHERE promotion_id = ?",
                Integer.class,
                promotionId
        );
        assertThat(actualVersion).isEqualTo(expectedVersion);
    }
}