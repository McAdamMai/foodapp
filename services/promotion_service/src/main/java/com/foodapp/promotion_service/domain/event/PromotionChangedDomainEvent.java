package com.foodapp.promotion_service.domain.event;

import com.foodapp.promotion_service.domain.model.PromotionDomain;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.domain.model.enums.AuditAction;
import com.foodapp.promotion_service.fsm.PromotionEvent;
import com.foodapp.promotion_service.fsm.UserRole;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class PromotionChangedDomainEvent {
    private final PromotionDomain oldPromotionDomain;
    private final PromotionDomain newPromotionDomain;

    // for audit log purpose

    private final String actor;
    private final UserRole role;
    private final AuditAction action;
    private final PromotionEvent event;
}
