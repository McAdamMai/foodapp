package com.foodapp.promotion_service.api.controller.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record TemplateDtoRequest(
        @NotBlank(message = "Name is required")
        String name,
        String description,
        @NotBlank(message = "Rule is required")
        // TODO: Change ruleJson to a dto based filed
        String ruleJson,
        @NotBlank(message = "Creator is required")
        String createdBy
) {
}
