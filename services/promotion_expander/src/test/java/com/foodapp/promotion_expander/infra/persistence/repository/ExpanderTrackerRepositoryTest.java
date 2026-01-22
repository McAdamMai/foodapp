package com.foodapp.promotion_expander.infra.persistence.repository;

import com.foodapp.promotion_expander.domain.model.PromotionRules;
import com.foodapp.promotion_expander.domain.model.enums.PromotionStatus;
import com.foodapp.promotion_expander.infra.persistence.entity.ExpanderTrackerEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ExpanderTrackerRepositoryTest {

    @Autowired
    private ExpanderTrackerRepository trackerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Tracker should act as a gatekeeper: Allow new versions, reject old ones")
    void testOptimisticLockingLifecycle() {
        // 1. ARRANGE
        UUID promotionId = UUID.randomUUID();

        // ======================================================
        // SCENARIO 1: First Event (Version 1) - Should Insert
        // ======================================================
        // We must build the FULL entity because columns like 'valid_start' are NOT NULL
        ExpanderTrackerEntity v1 = createDummyEntity(promotionId, 1);

        int rowsUpdated = trackerRepository.updateVersionIfNewer(v1);

        // Assert: Insert successful
        assertThat(rowsUpdated).isEqualTo(1);
        assertDbVersion(promotionId, 1);

        // ======================================================
        // SCENARIO 2: Valid Update (Version 5) - Should Update
        // ======================================================
        ExpanderTrackerEntity v5 = createDummyEntity(promotionId, 5);

        rowsUpdated = trackerRepository.updateVersionIfNewer(v5);

        // Assert: Update successful
        assertThat(rowsUpdated).isEqualTo(1);
        assertDbVersion(promotionId, 5);

        // ======================================================
        // SCENARIO 3: Stale Event (Version 3) - Should Ignore
        // ======================================================
        // Simulating out-of-order message
        ExpanderTrackerEntity v3 = createDummyEntity(promotionId, 3);

        rowsUpdated = trackerRepository.updateVersionIfNewer(v3);

        // Assert: Ignored (0 rows)
        assertThat(rowsUpdated).isEqualTo(0);
        // Assert: DB state remains at Version 5
        assertDbVersion(promotionId, 5);

        // ======================================================
        // SCENARIO 4: Duplicate Event (Version 5) - Should Ignore
        // ======================================================
        // Simulating duplicate message
        rowsUpdated = trackerRepository.updateVersionIfNewer(v5);

        // Assert: Ignored (0 rows)
        assertThat(rowsUpdated).isEqualTo(0);
    }

    // --- Helpers ---

    private ExpanderTrackerEntity createDummyEntity(UUID id, int version) {
        return ExpanderTrackerEntity.builder()
                .promotionId(id)
                .lastProcessedVersion(version)
                // Fill required fields to satisfy NOT NULL constraints
                .validStart(LocalDate.now())
                .validEnd(LocalDate.now().plusDays(10))
                .coveredUntil(null)
                .status(PromotionStatus.ACTIVE)
                .rules(new PromotionRules()) // Assuming empty constructor exists or use builder
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