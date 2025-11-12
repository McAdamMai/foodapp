package com.foodapp.promotion_service.domain.service;

import com.foodapp.promotion_service.api.controller.dto.request.PromotionUpdateRequest;
import com.foodapp.promotion_service.domain.exception.OptimisticLockException;
import com.foodapp.promotion_service.domain.mapper.PromotionMapper;
import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.fsm.PromotionEvent;
import com.foodapp.promotion_service.fsm.PromotionStateMachine;
import com.foodapp.promotion_service.fsm.PromotionStatus;
import com.foodapp.promotion_service.fsm.UserRole;
import com.foodapp.promotion_service.persistence.entity.PromotionEntity;
import com.foodapp.promotion_service.persistence.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final PromotionStateMachine promotionStateMachine;
    private final PromotionRepository promotionRepository;

    public PromotionDomain create(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            String createdBy,
            String temPlateId
    ){
        PromotionDomain newPromotion = PromotionDomain.createNew(
                name, description, startDate, endDate, createdBy, temPlateId);
        // no mappers are required because toEntity is set static (stateless)
        // static methods = shared tools, while instance methods = behavior tied to data.
        // instance belong to each object
        PromotionEntity entityToSave = PromotionMapper.toEntity(newPromotion);
        promotionRepository.save(entityToSave);
        return newPromotion;
    }

    /**
     * Submits a promotion for review.
     */
    public PromotionDomain submit(UUID id, String submittedBy){
        PromotionDomain domain = loadDomain(id);
        if(!domain.getCreatedBy().equals(submittedBy)) {
            throw new IllegalStateException("Only the creator can submit this promotion");
        }
        // actor will not be checked in SUBMIT
        // Validate transition through state machine
        PromotionStateMachine.TransitionResult result = promotionStateMachine.validateTransition(domain, PromotionEvent.SUBMIT, UserRole.CREATOR, submittedBy);

        // apply transition to get new domain
        PromotionDomain updateDomain = domain.applyTransition(result);

        // save and return
        promotionRepository.save(PromotionMapper.toEntity(domain));
        return updateDomain;
    }

    /**
     * Edits a rejected promotion back to draft.
     */
    public PromotionDomain edit(UUID id, String editedBy){
        String createdBy = loadDomain(id).getCreatedBy();

        // Verify editor is the creator
        if (!createdBy.equals(editedBy)) {
            throw new IllegalStateException("Only the creator can submit this promotion");
        }
        return executeTransition(id, PromotionEvent.EDIT, UserRole.CREATOR, editedBy);
    }

    @Transactional
    public PromotionDomain updateDetails(PromotionUpdateRequest request){
        // validation: must have something to update
        if (!request.hasUpdates()){
            throw new IllegalArgumentException("No fields to update");
        }

        // validation: update must be operated by creators
        PromotionDomain domain = loadDomain(request.getId());
        domain.validateCanBeEdited(request.getUpdatedBy());

        // Build update entity
        PromotionEntity updateEntity = buildUpdateEntity(request, domain.getVersion());

        // Execute update with optimistic locking
        int rowAffected = promotionRepository.updatePromotionDetails(updateEntity);

        if (rowAffected == 0) {
            throw new OptimisticLockException("Promotion was modified by another user or is not editable");
        }

        return loadDomain(request.getId());
    }

    public PromotionDomain approve(UUID id, String reviewedBy){
        return executeTransition(id, PromotionEvent.APPROVE, UserRole.REVIEWER, reviewedBy);
    }

    public PromotionDomain reject(UUID id, String reviewedBy){
        return executeTransition(id, PromotionEvent.REJECT, UserRole.REVIEWER, reviewedBy);
    }

    /**
     * Publishes an approved promotion.
     */
    public PromotionDomain publish(UUID id, String publishedBy){
        return executeTransition(id, PromotionEvent.PUBLISH, UserRole.PUBLISHER, publishedBy);
    }

    /**
     * Rolls back a published promotion.
     */
    public PromotionDomain rollBack(UUID id, String rollBackedBy, UserRole role){
        if (role != UserRole.ADMIN && role != UserRole.PUBLISHER) {
            throw new IllegalStateException("Only the admin and the publisher can roll back this promotion");
        }
        return executeTransition(id, PromotionEvent.PUBLISH, role, rollBackedBy);
    }

    /**
     * Gets available actions for a promotion based on user role.
     */
    public List<PromotionEvent> getAvailableActions(UUID id, UserRole role){
        PromotionStatus currentStatus = loadDomain(id).getStatus();
        return promotionStateMachine.getAvailableEvent(currentStatus, role);
    }

    /**
     * Gets all promotions for a promotion based on user role.
     */
    public List<PromotionDomain> findAll(){
        return promotionRepository.findAll()
                .stream()
                .map(PromotionMapper::toDomain)
                .toList();
    }

    public PromotionDomain findById(UUID id){
        return loadDomain(id);
    }

    // ========== PRIVATE HELPER ==========

    /**
     * Common method to execute any state transition.
     */

    private PromotionDomain executeTransition(UUID id, PromotionEvent event, UserRole userRole, String actor){
        PromotionDomain domain = loadDomain(id);

        // Validate transition
        PromotionStateMachine.TransitionResult result = promotionStateMachine.validateTransition(domain, event, userRole, actor);

        // apply transition to get the new domain
        PromotionDomain updateDomain = domain.applyTransition(result);

        // save and return
        promotionRepository.save(PromotionMapper.toEntity(updateDomain));
        return updateDomain;
    }

    private PromotionDomain loadDomain(UUID id) {
        return PromotionMapper.toDomain(
                promotionRepository.findById(id.toString())
        );
    }

    private PromotionEntity buildUpdateEntity (
            PromotionUpdateRequest request,
            Integer currentVersion
    ) {
        PromotionEntity.PromotionEntityBuilder builder = PromotionEntity.builder()
                .id(request.getId().toString())
                .version(currentVersion);

        // set only non-null fields
        if (request.getName() != null) {
            builder.name(request.getName());
        }
        if (request.getDescription() != null) {
            builder.description(request.getDescription());
        }
        if (request.getStartDate() != null) {
            builder.startDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            builder.endDate(request.getEndDate());
        }
        if (request.getTemplateId() != null) {
            builder.templateId(request.getTemplateId());
        }

        return builder.build();
    }
}
