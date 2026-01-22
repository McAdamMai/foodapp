package com.foodapp.promotion_expander.domain.model;

import com.foodapp.promotion_expander.domain.model.enums.PromotionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackerItem {
    private UUID promotionId;
    private int lastProcessedVersion;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate coverUntil;
    private PromotionStatus promotionStatus;
    private PromotionRules rules;
}
