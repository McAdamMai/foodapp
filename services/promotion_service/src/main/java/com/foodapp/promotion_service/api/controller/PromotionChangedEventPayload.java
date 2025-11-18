package com.foodapp.promotion_service.api.controller;

import com.foodapp.promotion_service.domain.model.enums.EventType;
import com.foodapp.promotion_service.domain.model.enums.MaskType;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

public record PromotionChangedEventPayload(
        UUID messageId,
        UUID promotionId,
        int promotionVersion,
        String status,
        List<MaskType> changeMask,
        Instant occurredAt,
        UUID correlationId
) { }
