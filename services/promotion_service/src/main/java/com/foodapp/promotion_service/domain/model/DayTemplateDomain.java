package com.foodapp.promotion_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayTemplateDomain {
    private UUID id;
    private String name;
    private String description;
    private String ruleJson;
    private String createBy;
    private LocalDateTime createAt;

    // creator
    public static DayTemplateDomain createNewTemplate (
            String name,
            String description,
            String ruleJson,
            String createBy){
        LocalDateTime now = LocalDateTime.now();
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
