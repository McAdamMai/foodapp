package com.foodapp.promotion_service.api.controller.dto.response;

import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.fsm.enums.PromotionStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PromotionSummaryDtoResponse(
        UUID id,
        String name,
        PromotionStatus status,
        OffsetDateTime startDate,
        OffsetDateTime endDate
) {
    public static PromotionSummaryDtoResponse from(PromotionDomain domain) {
        return new PromotionSummaryDtoResponse(
                domain.getId(),
                domain.getName(),
                domain.getStatus(),
                domain.getStartDate(),
                domain.getEndDate()
        );
    }
}
