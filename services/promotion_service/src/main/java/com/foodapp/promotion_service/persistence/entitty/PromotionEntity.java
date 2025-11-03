package com.foodapp.promotion_service.persistence.entitty;

import com.foodapp.promotion_service.fsm.PromotionStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A robust entity for MyBatis using constructor mapping.
 * Setters are private to enforce encapsulation. MyBatis will be configured
 * to use the all-arguments constructor to create instances.
 */

@Getter
@Setter(AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class PromotionEntity {
    private String id;
    private String name;
    private String description;
    private PromotionStatus status; // enums
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    private int version;
    private String createdBy;
    private String reviewedBy;
    private String publishedBy;
    private String templateId;
}
