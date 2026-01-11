package com.foodapp.promotion_service.domain.model;

import com.foodapp.promotion_service.domain.model.enums.MaskType;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.List;

public record PromotionChangedEventPayload(
        UUID messageId,
        UUID promotionId,
        int promotionVersion,

        String status,
        List<MaskType> changeMask,

        // THE NEW STATE (The Source of Truth)
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        UUID templateId,
        PromotionRules rules
) { }
