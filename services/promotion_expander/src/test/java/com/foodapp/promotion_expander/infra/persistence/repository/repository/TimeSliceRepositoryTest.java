package com.foodapp.promotion_expander.infra.persistence.repository.repository;

import com.foodapp.promotion_expander.infra.persistence.entity.TimeSliceEntity;
import com.foodapp.promotion_expander.infra.persistence.repository.TimeSliceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest // Spring Test + @Transactional = automatic rollback after each test
@Transactional  // ROLLS BACK changes after each test so your DB stays clean
class TimeSliceRepositoryTest {

    @Autowired
    private TimeSliceRepository repository;

    @Test
    void testInsertBatch_and_Find() {
        // 1. Arrange: Create two valid entities for the same promotion
        UUID promotionId = UUID.randomUUID();
        TimeSliceEntity slice1 = createCompleteEntity(promotionId, "DISCOUNT", 15.0);
        TimeSliceEntity slice2 = createCompleteEntity(promotionId, "DISCOUNT", 15.0);

        // 2. Act: Insert them
        repository.insertBatch(List.of(slice1, slice2));

        // 3. Assert: Retrieve and verify every field maps correctly
        List<TimeSliceEntity> results = repository.findByPromotionId(promotionId);

        assertThat(results).hasSize(2);

        // Deep verification of the first item to ensure mapping isn't losing data
        TimeSliceEntity resultSlice = results.get(0);
        assertThat(resultSlice.getPromotionId()).isEqualTo(promotionId);
        assertThat(resultSlice.getVersion()).isEqualTo(1);
        assertThat(resultSlice.getTimezone()).isEqualTo("America/New_York");
        assertThat(resultSlice.getEffectType()).isEqualTo("DISCOUNT");
        assertThat(resultSlice.getSliceDate()).isNotNull();
        assertThat(resultSlice.getStartTime()).isNotNull();
    }

    @Test
    void testUpdateContent_ShouldOnlyUpdateRules_NotDates() {
        // 1. Arrange: Insert initial data
        UUID promotionId = UUID.randomUUID();
        TimeSliceEntity original = createCompleteEntity(promotionId, "OLD_TYPE", 10.0);
        repository.insertBatch(List.of(original));

        // 2. Act: Create an update payload (Time/Date fields are NULL)
        TimeSliceEntity updates = new TimeSliceEntity();
        updates.setEffectType("NEW_FANCY_TYPE");
        updates.setEffectValue(99.9);
        updates.setVersion(2);

        repository.updateContentByPromotionId(promotionId, updates);

        // 3. Assert: Verify logic
        List<TimeSliceEntity> results = repository.findByPromotionId(promotionId);
        TimeSliceEntity updated = results.get(0);

        // CHANGED: The effect fields should be updated
        assertThat(updated.getEffectType()).isEqualTo("NEW_FANCY_TYPE");
        assertThat(updated.getEffectValue()).isEqualTo(99.9);
        assertThat(updated.getVersion()).isEqualTo(2);

        // UNCHANGED: The dates should be exactly as they were before
        assertThat(updated.getSliceDate()).isEqualTo(original.getSliceDate());
        assertThat(updated.getStartTime()).isEqualTo(original.getStartTime());
    }

    @Test
    void testDeleteSlices() {
        // 1. Arrange
        UUID promotionId = UUID.randomUUID();
        repository.insertBatch(List.of(createCompleteEntity(promotionId, "DEL", 1.0)));

        // 2. Act
        repository.deleteSlicesByPromotionId(promotionId);

        // 3. Assert
        List<TimeSliceEntity> results = repository.findByPromotionId(promotionId);
        assertThat(results).isEmpty();
    }

    // --- Helper Method ---
    private TimeSliceEntity createCompleteEntity(UUID promotionId, String effectType, Double effectValue) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS); // Truncate to avoid millisecond mismatch in DB comparisons

        return TimeSliceEntity.builder()
                .id(UUID.randomUUID())
                .promotionId(promotionId)
                .version(1)
                .sliceDate(LocalDate.now())
                .startTime(now)
                .endTime(now.plus(1, ChronoUnit.HOURS))
                .timezone("America/New_York")
                .effectType(effectType)
                .effectValue(effectValue)
                .build();
    }
}