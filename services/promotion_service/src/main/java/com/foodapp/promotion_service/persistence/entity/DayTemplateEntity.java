package com.foodapp.promotion_service.persistence.entity;

import com.foodapp.promotion_service.domain.model.DayTemplateDomain;
import com.foodapp.promotion_service.domain.model.PromotionRules;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID; // Import UUID

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DayTemplateEntity {
    // 1. CHANGE THIS FROM String TO UUID
    private UUID id;

    private String name;
    private String description;
    private PromotionRules ruleJson;
    private String createdBy;
    private OffsetDateTime createdAt;

}