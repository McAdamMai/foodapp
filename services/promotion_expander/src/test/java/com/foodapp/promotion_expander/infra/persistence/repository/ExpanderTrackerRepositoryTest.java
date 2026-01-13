package com.foodapp.promotion_expander.infra.persistence.repository;

import com.foodapp.promotion_expander.infra.persistence.entity.ExpanderTrackerEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ExpanderTrackerRepositoryTest {

    @Autowired
    private ExpanderTrackerRepository trackerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate; // Used to verify DB state directly

    @Test
    @DisplayName("Tracker should act as a gatekeeper: Allow new versions, reject old ones")
    void testOptimisticLockingLifecycle() {
        // 1. ARRANGE
        UUID promotionId = UUID.randomUUID();

        // ======================================================
        // SCENARIO 1: First Event (Version 1) - Should Insert
        // ======================================================
        int rowsUpdated = trackerRepository.updateVersionIfNewer(promotionId, 1);

        // Assert: Operation successful (1 row affected)
        assertThat(rowsUpdated).isEqualTo(1);
        // Assert: DB contains version 1
        assertDbVersion(promotionId, 1);

        // ======================================================
        // SCENARIO 2: Valid Update (Version 5) - Should Update
        // ======================================================
        rowsUpdated = trackerRepository.updateVersionIfNewer(promotionId, 5);

        // Assert: Operation successful (1 row affected)
        assertThat(rowsUpdated).isEqualTo(1);
        // Assert: DB now holds version 5
        assertDbVersion(promotionId, 5);

        // ======================================================
        // SCENARIO 3: Stale Event (Version 3) - Should Ignore
        // ======================================================
        // This simulates an out-of-order message arriving late
        rowsUpdated = trackerRepository.updateVersionIfNewer(promotionId, 3);

        // Assert: Operation IGNORED (0 rows affected)
        assertThat(rowsUpdated).isEqualTo(0);
        // Assert: DB still holds version 5 (Has not been downgraded)
        assertDbVersion(promotionId, 5);

        // ======================================================
        // SCENARIO 4: Duplicate Event (Version 5) - Should Ignore
        // ======================================================
        // This simulates "At-Least-Once" delivery (same message twice)
        rowsUpdated = trackerRepository.updateVersionIfNewer(promotionId, 5);

        // Assert: Operation IGNORED (0 rows affected)
        assertThat(rowsUpdated).isEqualTo(0);
    }

    // Helper to peek into the DB without using the Repository (ensures truth)
    private void assertDbVersion(UUID promotionId, int expectedVersion) {
        Integer actualVersion = jdbcTemplate.queryForObject(
                "SELECT last_processed_version FROM expander_tracker WHERE promotion_id = ?",
                Integer.class,
                promotionId
        );
        assertThat(actualVersion).isEqualTo(expectedVersion);
    }
}