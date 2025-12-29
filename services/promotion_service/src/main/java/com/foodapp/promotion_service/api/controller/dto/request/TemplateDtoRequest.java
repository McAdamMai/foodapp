package com.foodapp.promotion_service.api.controller.dto.request;

import com.foodapp.promotion_service.domain.model.PromotionRules;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TemplateDtoRequest(
        @NotBlank(message = "Name is required")
        String name,
        String description,
        @NotNull(message = "Rule is required")
        @Valid
        PromotionRules ruleJson,
        @NotBlank(message = "Creator is required")
        String createdBy
) {
}
