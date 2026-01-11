package com.foodapp.promotion_service.infra.repository;

import com.foodapp.promotion_service.domain.mapper.PromotionMapper;
import com.foodapp.promotion_service.domain.model.PromotionChangedEventPayload;
import com.foodapp.promotion_service.domain.model.PromotionOutboxDomain;
import com.foodapp.promotion_service.domain.model.PromotionRules;
import com.foodapp.promotion_service.domain.model.enums.MaskType;
import com.foodapp.promotion_service.persistence.entity.PromotionOutboxEntity;
import com.foodapp.promotion_service.persistence.repository.PromotionOutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// 1. Load the full context (MyBatis Mappers + DB Config)
@SpringBootTest
// 2. Rollback transaction after test finishes (Clean DB)
@Transactional
class PromotionOutboxRepositoryTest {

    @Autowired
    private PromotionOutboxRepository repository;

    @Test
    @DisplayName("Should save Domain with Object Payload and retrieve it correctly deserialized")
    void testSaveAndRetrieveOutbox() {
        // ============================================
        // 1. PREPARE DATA
        // ============================================
        UUID promotionId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID(); // Fixed ID for easier assertion

        PromotionChangedEventPayload payload = new PromotionChangedEventPayload(
                messageId,
                promotionId,
                1,
                "PUBLISHED",
                List.of(MaskType.SCHEDULE, MaskType.EFFECT),
                OffsetDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 1, 31, 23, 59, 59, 0, ZoneOffset.UTC),
                templateId,
                new PromotionRules()
        );

        PromotionOutboxDomain originalDomain = PromotionOutboxDomain.builder()
                .id(messageId) // Use the same ID
                .aggregateId(promotionId)
                .aggregateVersion(1)
                .changeMask(List.of("SCHEDULE", "EFFECT"))
                // .eventType("PROMOTION_CHANGED") <--- REMOVED (You removed this from DB/Domain earlier)
                .payload(payload)
                .publishedAt(null)
                .build();

        // ============================================
        // 2. EXECUTE (MyBatis Style)
        // ============================================

        // A. Convert
        PromotionOutboxEntity entityToSave = PromotionMapper.toEntity(originalDomain);

        // B. Insert using YOUR Mapper Method (not .save())
        repository.createOutbox(entityToSave);

        // C. Retrieve using YOUR Mapper Method (not .findById())
        // Since we don't have findById in XML yet, we use findPendingBatch
        List<PromotionOutboxEntity> pendingBatch = repository.findPendingBatch(10);

        // ============================================
        // 3. ASSERTIONS
        // ============================================

        // Find our specific item in the batch
        PromotionOutboxEntity retrievedEntity = pendingBatch.stream()
                .filter(e -> e.getId().equals(messageId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Saved entity not found in DB"));

        PromotionOutboxDomain retrievedDomain = PromotionMapper.toDomain(retrievedEntity);

        // Verify Data Integrity
        assertThat(retrievedDomain).isNotNull();
        assertThat(retrievedDomain.getId()).isEqualTo(messageId);

        // Verify Payload Serialization/Deserialization
        assertThat(retrievedDomain.getPayload()).isNotNull();
        assertThat(retrievedDomain.getPayload().messageId()).isEqualTo(messageId);
        assertThat(retrievedDomain.getPayload().status()).isEqualTo("PUBLISHED");

        // Verify Date Handling inside JSON
        assertThat(retrievedDomain.getPayload().startDate())
                .isEqualTo(payload.startDate());

        System.out.println("Test Passed: Payload serialized/deserialized correctly via MyBatis.");
    }
}