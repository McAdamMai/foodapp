package com.foodapp.promotion_service.persistence.entity;

import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true) // allow a new instance of an existing object with some fields changed, without copying all the fields
@AllArgsConstructor
@NoArgsConstructor
public class PromotionScopeEntity {
    private String scopeId;
    private String promotionId;
    private String scopeType;
}
