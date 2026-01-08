package com.foodapp.promotion_service.fsm;

import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.fsm.enums.PromotionEvent;
import com.foodapp.promotion_service.fsm.enums.PromotionStatus;
import com.foodapp.promotion_service.fsm.enums.UserRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromotionStateMachine {
    private final List<Transition> transitions = List.of(
            new Transition(PromotionStatus.DRAFT, PromotionEvent.EDIT, PromotionStatus.DRAFT, UserRole.CREATOR),
            new Transition(PromotionStatus.DRAFT, PromotionEvent.SUBMIT, PromotionStatus.SUBMITTED, UserRole.CREATOR),
            new Transition(PromotionStatus.SUBMITTED, PromotionEvent.APPROVE, PromotionStatus.APPROVED, UserRole.REVIEWER),
            new Transition(PromotionStatus.SUBMITTED, PromotionEvent.REJECT, PromotionStatus.REJECTED, UserRole.REVIEWER),
            new Transition(PromotionStatus.APPROVED, PromotionEvent.PUBLISH, PromotionStatus.PUBLISHED, UserRole.PUBLISHER),
            new Transition(PromotionStatus.PUBLISHED, PromotionEvent.ROLLBACK, PromotionStatus.ROLLED_BACK, UserRole.ADMIN, UserRole.PUBLISHER),
            new Transition(PromotionStatus.REJECTED, PromotionEvent.EDIT, PromotionStatus.DRAFT, UserRole.CREATOR)
    );

    /**
     * Result of a state transition validation.
     */
    @Getter
    @RequiredArgsConstructor
    public static class TransitionResult {
        private final PromotionStatus newStatus;
        private final PromotionEvent event;
        private final String actor;
    }

    public TransitionResult validateTransition(PromotionDomain domain, PromotionEvent event, UserRole role, String actor){

        Transition validTransition = transitions.stream()
                // If a legal transition or not
                .filter(t -> t.matches(domain.getStatus(), event, role))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid transition for status=" + domain.getStatus() + ", event=" + event + ", role=" + role));

        return new TransitionResult(validTransition.getTo(), event, actor);
    }

    /**
     * Checks if a transition is valid without executing it.
     */
    public boolean canTransition(PromotionStatus status, PromotionEvent event, UserRole role){
        return transitions.stream()
                .anyMatch(t -> t.matches(status, event, role));
    }

    /**
     * Checks if a transition is valid without executing it.
     */
    public List<PromotionEvent> getAvailableEvent(PromotionStatus currentStatus, UserRole role){
        return transitions.stream()
                .filter(t -> t.getFrom() == currentStatus && t.isAllowedFor(role))
                .map(Transition::getEvent) // get all events that fall onto the filter
                .toList();
    }
}
