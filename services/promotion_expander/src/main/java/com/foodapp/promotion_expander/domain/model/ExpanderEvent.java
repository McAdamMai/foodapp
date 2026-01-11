package com.foodapp.promotion_expander.domain.model;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    @JsonAlias("promotionVersion")
    private int version;
    private UUID messageId;
    // Trigger
    private List<MaskType> changeMask;
    @JsonAlias("status")
    private String promotionStatus;
    // Context
    @JsonAlias("startDate")
    private OffsetDateTime startDateTime;
    @JsonAlias("endDate")
    private OffsetDateTime endDateTime;
    private PromotionRules rules;
}
