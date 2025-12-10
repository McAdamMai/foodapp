package com.foodapp.promotion_service.api.controller.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record PromotionCreationDtoRequest(
        @NotBlank(message = "Name is required")
        String name,

        String description,

        @NotNull(message = "Start date is required")
        @Future(message = "Start date must be in the future")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        @NotBlank(message = "Creator ID is required")
        String createdBy,

        @NotNull(message = "Template ID is required")
        UUID templateId
){
    // Validation
    public PromotionCreationDtoRequest {
        // FIX 1: Add "startDate != null" check to avoid 500 Server Error on missing data
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            // FIX 2: Correct the message to say "after", not "before"
            throw new IllegalArgumentException("End date must be after start date");
        }
    }
}