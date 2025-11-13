package com.foodapp.promotion_service.domain.model;

import com.foodapp.promotion_service.domain.model.enums.ReviewDecision;
import com.foodapp.promotion_service.fsm.PromotionStateMachine;
import com.foodapp.promotion_service.fsm.PromotionStatus;
import com.foodapp.promotion_service.fsm.PromotionStateMachine.TransitionResult;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDomain {
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
    private String templateId;

    // ========== FACTORY METHOD FOR CREATION ==========
    /**
     * Creates a new promotion in DRAFT status.
     * This is the controlled entry point for creation.
     */
    // generate a domain from user's data
    public static PromotionDomain createNew(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            String createdBy,
            String templateId
    ){
        LocalDateTime now = LocalDateTime.now();
        return PromotionDomain.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .status(PromotionStatus.DRAFT)
                .startDate(startDate)
                .endDate(endDate)
                .createAt(now)
                .updateAt(now)
                .version(1)
                .createdBy(createdBy)
                .templateId(templateId)
                .build();
    }

    /**
     * Applies a state transition result to create a new domain instance.
     * This respects immutability by returning a new object.
     *
     * @param transitionResult the validated transition from the state machine
     * @return new PromotionDomain with updated state
     */

    public PromotionDomain applyTransition(TransitionResult transitionResult) {
        PromotionDomainBuilder builder = this.toBuilder()
                .status(transitionResult.getNewStatus())
                .updateAt(LocalDateTime.now());

        // ✅ Clean, safe, modern
        switch (transitionResult.getEvent()) {
            case APPROVE, REJECT -> builder.reviewedBy(transitionResult.getActor());
            case PUBLISH -> builder.publishedBy(transitionResult.getActor());
            case SUBMIT, ROLLBACK -> { /* No actor updates */ }
        }

        return builder.build();
    }

    // === SUBMIT ====
    public void validateCanBeSubmitted(String submittedBy) {
        if (this.status != PromotionStatus.DRAFT) {
            throw new IllegalStateException("Can only submit DRAFT promotion, Current: +" + this.status);
        }
        if (!this.createdBy.equals(submittedBy)) {
            throw new IllegalStateException("Only the creator can submit this promotion");
        }
    }

    public void validateCanBeEdited(String UpdatedBy){
        if (this.status != PromotionStatus.DRAFT &&
        this.status != PromotionStatus.REJECTED) {
            throw new IllegalStateException("Only DRAFT or REJECTED promotions can be edited");
        }
        if (!this.createdBy.equals(UpdatedBy)) {
            throw new IllegalStateException("Only the creator can edit this promotion");
        }
    }
}
