package com.foodapp.promotion_service.fsm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class Transition {
    private final PromotionStatus from;
    private final PromotionStatus to;
    private final PromotionEvent event;
    private final Set<UserRole> roles;

    // define transition rules
    public Transition(PromotionStatus from, PromotionEvent event, PromotionStatus to, UserRole ... allowed) {
        this.from = from;
        this.to = to;
        this.event = event;
        this.roles = Set.of(allowed);
    }

    // verify the validity of transition
    public boolean matches(PromotionStatus currentStatus, PromotionEvent event, UserRole role) {
        return this.from == currentStatus && this.event == event && isAllowedFor(role);
    }

    public boolean isAllowedFor(UserRole role) {
        return this.roles.contains(role);
    }
}
