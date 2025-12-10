package com.foodapp.promotion_service.persistence.entity;

import com.foodapp.promotion_service.fsm.PromotionStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

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
    private UUID id;
    private String name;
    private String description;
    private PromotionStatus status; // enums
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    // Optimistic lock field
    private int version;

    private String createdBy;
    private String reviewedBy;
    private String publishedBy;
    private UUID templateId;
    // missing scopeId

    /**
     * Factory method for creating partial update entities.
     * Only sets id and version, other fields can be set via setters.
     */
    @Deprecated
    public static PromotionEntity forUpdate(UUID id, Integer version) {
        PromotionEntity entity = new PromotionEntity();
        entity.id = id;
        entity.version = version;
        return entity;
    }
}
