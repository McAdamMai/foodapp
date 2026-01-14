package com.foodapp.promotion_expander.infra.persistence.entity;

import com.foodapp.promotion_expander.infra.persistence.entity.enuns.PromotionStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpanderTrackerEntity {
    private UUID promotionId;
    private Integer lastProcessedVersion;
    private Instant updatedAt;
    private Instant validStart;
    private Instant validEnd;
    private Instant coveredUntil;
    private PromotionStatus status;
}
