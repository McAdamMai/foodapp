package com.foodapp.promotion_service.persistence.entity;

import lombok.*;

import java.time.LocalDateTime;
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
    private String ruleJson;
    private String createdBy;
    private LocalDateTime createdAt;
}