package com.foodapp.promotion_service.api.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Future;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PromotionUpdateRequest(
        UUID id,
        Integer version,

        String name,
        String description,

        @Future(message = "Start date must be in the future")
        LocalDate startDate,

        LocalDate endDate,
        UUID templateId,

        // Required field
        String updatedBy
) {

    /**
     * Check if this request has any fields to update.
     * This works perfectly fine in a record.
     */
    public boolean hasUpdates() {
        return name != null ||
                description != null ||
                startDate != null ||
                endDate != null ||
                templateId != null;
    }

    /**
     * Compact Constructor for Validation.
     * CRITICAL FIX: We must handle partial updates safely.
     */
    public PromotionUpdateRequest {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }
}