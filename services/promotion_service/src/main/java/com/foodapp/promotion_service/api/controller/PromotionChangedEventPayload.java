package com.foodapp.promotion_service.api.controller;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

public record PromotionChangedEventPayload(
        UUID messageId,
        UUID promotionId,
        int promotionVersion,
        String status,
        List<String> changeMask,
        Instant occurredAt,
        String correlationId
) { }
