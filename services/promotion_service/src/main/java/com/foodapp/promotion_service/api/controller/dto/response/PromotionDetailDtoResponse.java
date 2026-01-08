package com.foodapp.promotion_service.api.controller.dto.response;

import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.fsm.enums.PromotionStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PromotionDetailDtoResponse(
        UUID id,
        String name,
        String description,
        PromotionStatus status,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        int version,
        String createdBy,
        String reviewedBy,
        String publishedBy,
        UUID templateId
) {
    public static PromotionDetailDtoResponse from(PromotionDomain domain) {
        return new PromotionDetailDtoResponse(
                domain.getId(),
                domain.getName(),
                domain.getDescription(),
                domain.getStatus(),
                domain.getStartDate(),
                domain.getEndDate(),
                domain.getCreateAt(),
                domain.getUpdateAt(),
                domain.getVersion(),
                domain.getCreatedBy(),
                domain.getReviewedBy(),
                domain.getPublishedBy(),
                domain.getTemplateId()
        );
    }
}
