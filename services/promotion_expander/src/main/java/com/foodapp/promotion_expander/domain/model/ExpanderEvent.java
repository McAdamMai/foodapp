package com.foodapp.promotion_expander.domain.model;

import com.foodapp.promotion_expander.domain.model.enums.MaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// The model extracted from PromotionService's payload
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpanderEvent {
    // Identity
    private UUID promotionId;
    private int version;
    private UUID messageId;
    // Trigger
    private List<MaskType> changeMask;
    private String PromotionStatus;
    // Context
    private OffsetDateTime startDateTime;
    private OffsetDateTime endDateTime;
    private PromotionRules rules;
}
