package com.foodapp.promotion_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayTemplateDomain {
    private UUID id;
    private String name;
    private String description;
    private PromotionRules ruleJson;
    private String createBy;
    private OffsetDateTime createAt;

    // creator
    public static DayTemplateDomain createNewTemplate (
            String name,
            String description,
            PromotionRules ruleJson,
            String createBy){
            OffsetDateTime now = OffsetDateTime.now();
        return DayTemplateDomain.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .ruleJson(ruleJson)
                .createBy(createBy)
                .createAt(now)
                .build();
    }
}
