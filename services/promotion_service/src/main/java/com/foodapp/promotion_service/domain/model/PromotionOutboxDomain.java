package com.foodapp.promotion_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class PromotionOutboxDomain {
    private UUID id;
    private UUID aggregateId;
    private int aggregateVersion;
    private List<String> changeMask;
    private String eventType;
    private String payload;
    private Instant publishedAt;

    // ========== FACTORY METHOD FOR CREATION ==========
    /**
     * Creates a new promotion outbox.
     * This is the controlled entry point for creation.
     */
    // generate a domain from user's data
    public static PromotionOutboxDomain createOutbox(
            UUID id,
            UUID aggregateId,
            int aggregateVersion,
            List<String> changeMask,
            String eventType,
            String payload
    ){
        Instant now = Instant.now();
        return PromotionOutboxDomain.builder()
                .id(id)
                .aggregateId(aggregateId)
                .aggregateVersion(aggregateVersion)
                .changeMask(changeMask)
                .eventType(eventType)
                .payload(payload)
                .publishedAt(null)
                .build();
    }

}
