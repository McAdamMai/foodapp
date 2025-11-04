package com.foodapp.promotion_service.api.controller.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record PromotionCreationDtoRequest(
        @NotBlank(message = "Name is required")
        String name,
        String description,
        @NotBlank(message = "Start date is required")
        @Future(message = "Start date must be in the future")
        LocalDate startDate,
        @NotBlank(message = "End date is required")
        LocalDate endDate,
        @NotBlank(message = "Creator ID is required")
        String createdBy,
        @NotBlank(message = "Template ID is required")
        String templateId
){
    // Validation
    public PromotionCreationDtoRequest {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be before start date");
        }
    }
}
