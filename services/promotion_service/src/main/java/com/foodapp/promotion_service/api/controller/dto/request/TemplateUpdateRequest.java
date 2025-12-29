package com.foodapp.promotion_service.api.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.foodapp.promotion_service.domain.model.PromotionRules;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateUpdateRequest {
    private UUID id;
    private String name;
    private String description;
    private PromotionRules ruleJson;
    private String createdBy;

    /**
     * Check if this request has any fields t update (can it be done by frontend)
     * */

    public boolean hasUpdate () {
        return id != null || name != null || description != null || ruleJson != null || createdBy != null;
    }
}
