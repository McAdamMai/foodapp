package com.foodapp.promotion_service.domain.model;

import com.foodapp.promotion_service.exception.UnauthorizedException;
import com.foodapp.promotion_service.fsm.enums.PromotionStatus;
import com.foodapp.promotion_service.fsm.PromotionStateMachine.TransitionResult;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDomain {
    // meta data
    private UUID id;
    private String name;
    private String description;
    private String createdBy;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private OffsetDateTime createAt;
    private OffsetDateTime updateAt;
    private UUID templateId;
    private PromotionRules jsonRules;
    // Optimistic lock field
    private int version;
    // The change of this data must go through FSM
    // The verification chain: domain.validate -> promotionStateMachine.validate -> domain.transition
    private PromotionStatus status; // enums
    private String reviewedBy;
    private String publishedBy;

    // ========== FACTORY METHOD FOR CREATION ==========
    /**
     * Creates a new promotion in DRAFT status.
     * This is the controlled entry point for creation.
     */
    // generate a domain from user's data
    public static PromotionDomain createNew(
            String name,
            String description,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String createdBy,
            UUID templateId,
            PromotionRules jsonRules
    ){
        OffsetDateTime now = OffsetDateTime.now();
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
                .jsonRules(jsonRules)
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
                .updateAt(OffsetDateTime.now());

        //  Clean, safe, modern
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

    public void validateCanBeReviewed(String reviewedBy) {
        if (this.status != PromotionStatus.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED promotions can be reviewed");
        }

        if (this.createdBy.equals(reviewedBy)) {
            throw new UnauthorizedException("User %s is not authorized to review this promotion");
        }
    }

    public void validateCanBePublished(String publishedBy) {
        if (this.status != PromotionStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED promotions can be published");
        }

        if (this.createdBy.equals(publishedBy)) {
            throw new IllegalStateException("Only the publisher can publish this promotion");
        }
    }

    public void validateRollback() {
        if (this.status != PromotionStatus.PUBLISHED) {
            throw new IllegalStateException("Only PUBLISHED promotions can be rollbacked");
        }
        // should have identity check?
    }

    public void validateCanBeUpdated(String UpdatedBy){
        if (this.status != PromotionStatus.PUBLISHED) {
            throw new IllegalStateException("Only PUBLISH can be urgently updated");
        }
    }
}
