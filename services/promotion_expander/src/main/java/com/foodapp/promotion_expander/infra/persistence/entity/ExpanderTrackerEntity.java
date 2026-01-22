package com.foodapp.promotion_expander.infra.persistence.entity;

import com.foodapp.promotion_expander.domain.model.PromotionRules;
import com.foodapp.promotion_expander.domain.model.enums.PromotionStatus;
import io.swagger.v3.core.util.Json;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpanderTrackerEntity {
    private UUID promotionId;
    private Integer lastProcessedVersion;
    private Instant updatedAt;
    private LocalDate validStart;
    private LocalDate validEnd;
    private LocalDate coveredUntil;
    private PromotionStatus status;
    private PromotionRules rules;
}
