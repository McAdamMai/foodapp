package com.foodapp.promotion_service.persistence.entity;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class PromotionOutboxEntity {
    private UUID id;
    private UUID aggregateId;
    private int aggregateVersion;
    private String eventType;
    private List<String> changeMask;
    private String payload;
    private Instant occurredAt;
    private Instant publishedAt;
}
