package com.foodapp.promotion_service.domain.event;

import com.foodapp.promotion_service.domain.model.PromotionDomain;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class PromotionChangedDomainEvent {
    private final PromotionDomain oldPromotionDomain;
    private final PromotionDomain newPromotionDomain;
    private final UUID correlationId;
}
