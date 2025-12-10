package com.foodapp.promotion_service.api.controller;

import com.foodapp.promotion_service.domain.model.enums.EventType;
import com.foodapp.promotion_service.domain.model.enums.MaskType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.List;

public record PromotionChangedEventPayload(
        UUID messageId,
        UUID promotionId,
        int promotionVersion,
        String status,
        List<MaskType> changeMask,

        // THE NEW STATE (The Source of Truth)
        LocalDate startDate,
        LocalDate endDate,
        UUID templateId
) { }
