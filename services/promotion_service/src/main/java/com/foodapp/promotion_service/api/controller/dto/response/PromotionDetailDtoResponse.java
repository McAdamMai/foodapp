package com.foodapp.promotion_service.api.controller.dto.response;

import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.fsm.PromotionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PromotionDetailDtoResponse(
        UUID id,
        String name,
        String description,
        PromotionStatus status,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int version,
        String createdBy,
        String reviewedBy,
        String publishedBy,
        String templateId
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
