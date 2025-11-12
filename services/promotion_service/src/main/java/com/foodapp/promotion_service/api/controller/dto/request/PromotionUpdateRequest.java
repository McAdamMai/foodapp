package com.foodapp.promotion_service.api.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PromotionUpdateRequest {
    private UUID id;
    private Integer version;

    // Optional fields - null  means "don't update"
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String templateId;

    // Required field
    private String updatedBy;

    /**
     * Check if this request has any fields t update (can it be done by frontend)
     * */
    public boolean hasUpdates() {
        return name != null ||
                description != null ||
                startDate != null ||
                endDate != null ||
                templateId != null;
    }
}
