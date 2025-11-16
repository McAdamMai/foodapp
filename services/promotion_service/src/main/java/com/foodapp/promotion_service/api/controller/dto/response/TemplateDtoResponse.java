package com.foodapp.promotion_service.api.controller.dto.response;

import com.foodapp.promotion_service.domain.model.DayTemplateDomain;

import java.util.UUID;

public record TemplateDtoResponse(
        UUID id,
        String name,
        String ruleJson,
        String createdBy
) {

    public static TemplateDtoResponse from(DayTemplateDomain domain) {
        return new TemplateDtoResponse(
                domain.getId(),
                domain.getName(),
                domain.getRuleJson(),
                domain.getCreateBy()
        );
    }
}
