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

    // TODO: how to identify create and modify promotion(from publish)
    // TODO: https://poe.com/s/RQPoYfAoXUd8uyto2TtC kafka dispatcher
    @Transactional
    public PromotionDomain create(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            String createdBy,
            String temPlateId
    ){
        // add log for "log.info("Creating new promotion: name={}, createdBy={}", name, createdBy)"
        PromotionDomain newPromotion = PromotionDomain.createNew(
                name, description, startDate, endDate, createdBy, temPlateId);
        // no mappers are required because toEntity is set static (stateless)
        // static methods = shared tools, while instance methods = behavior tied to data.
        // instance belong to each object
        PromotionEntity entityToSave = PromotionMapper.toEntity(newPromotion);
        promotionRepository.save(entityToSave);

        // add log for "log.info("Promotion created successfully: id={}", newPromotion.getId());"
        return newPromotion;
    }

    @Transactional
    public PromotionDomain updateDetails(PromotionUpdateRequest request){
        // validation: must have something to update
        if (!request.hasUpdates()){
            throw new IllegalArgumentException("No fields to update");
        }

        // 1. Load current state
        PromotionDomain domain = loadDomain(request.getId());

        // 2. Validate (read-only)
        domain.validateCanBeEdited(request.getUpdatedBy());

        // 3. Build update entity with CURRENT version
        PromotionEntity updateEntity = buildUpdateEntity(request, domain.getVersion());

        // 4. Execute atomic update with optimistic locking
        int rowAffected = promotionRepository.updatePromotionDetails(updateEntity);

        // 5. Handle optimistic lock conflict
        if (rowAffected == 0) {
            throw new OptimisticLockException("Promotion was modified by another user or is not editable");
        }

        // 6. Reload and return updated state
        return loadDomain(request.getId());
    }

    /**
     * Submits a promotion for review.
     */
    @Transactional
    public PromotionDomain submit(UUID id, String submittedBy){

        // 1. load current state
        PromotionDomain currentDomain = loadDomain(id);

        // 2. validate business rules (read-only, doesn't modify state)
        currentDomain.validateCanBeSubmitted(submittedBy);
        // 3. Execute transactional state transition
        return executeTransition(
          currentDomain,
          PromotionEvent.SUBMIT,
          UserRole.CREATOR,
          submittedBy
        );
    }

    /**
     * approve a promotion for review.
     */
    @Transactional
    public PromotionDomain approve(UUID id, String reviewedBy){
        // add log log.info("Approving promotion: id={}, reviewedBy={}", id, reviewedBy);
        // 1. load current state
        PromotionDomain currentDomain = loadDomain(id);

        currentDomain.validateCanBeReviewed(reviewedBy);

        return executeTransition(
            currentDomain,
            PromotionEvent.APPROVE,
            UserRole.REVIEWER,
            reviewedBy
        );
    }

    /**
     * reject a promotion for review.
     */
    @Transactional
    public PromotionDomain reject(UUID id, String reviewedBy){
        // add log log.info("Rejecting promotion: id={}, reviewedBy={}", id, reviewedBy);
        PromotionDomain currentDomain = loadDomain(id);

        currentDomain.validateCanBeReviewed(reviewedBy);

        return executeTransition(
                loadDomain(id),
                PromotionEvent.REJECT,
                UserRole.REVIEWER,
                reviewedBy
        );
    }

    /**
     * Publishes an approved promotion.
     */
    @Transactional
    public PromotionDomain publish(UUID id, String publishedBy){
        // fetch current status
        PromotionDomain currentDomain = loadDomain(id);

        currentDomain.validateCanBePublished(publishedBy);

        return executeTransition(
                loadDomain(id),
                PromotionEvent.PUBLISH,
                UserRole.PUBLISHER,
                publishedBy
        );

    }

    /**
     * Rolls back a published promotion.
     */
    @Transactional
    public PromotionDomain rollBack(UUID id, String rolledBackBy, UserRole role){
        PromotionDomain currentDomain = loadDomain(id);

        currentDomain.validateRollback();

        return executeTransition(
                loadDomain(id),
                PromotionEvent.ROLLBACK,
                role,
                rolledBackBy
        );
    }

    // ========== QUERIES ==========
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

    // ========== PRIVATE HELPERS ==========

    /**
     *  CORE TRANSACTIONAL METHOD: Executes state transition atomically
     *
     * Flow:
     * 1. Validate transition through FSM (in-memory, safe)
     * 2. Create new domain with new state (immutable, safe)
     * 3. Update DB with optimistic locking (atomic, transactional)
     * 4. If DB update fails → transaction rolls back automatically
     * 5. If DB update succeeds → reload and return new state
     */


    private PromotionDomain executeTransition(
            PromotionDomain currentDomain,
            PromotionEvent event,
            UserRole userRole,
            String actor){
        // 1. validate transaction through FSM (just for verification, will not modify anything)
        PromotionStateMachine.TransitionResult result = promotionStateMachine.validateTransition(
                currentDomain, event, userRole, actor
        );

        // 2. Create a new domain state (immutable doesn't modify currentDomain)
        PromotionDomain updatedDomain  = currentDomain.applyTransition(result);

        // 3. Execute atomic DB update with optimistic locking
        int rowsAffected = promotionRepository.updateStateTransition(
                currentDomain.getId().toString(),
                updatedDomain.getStatus(),
                currentDomain.getStatus(),
                currentDomain.getVersion(),
                updatedDomain.getReviewedBy(),
                updatedDomain.getPublishedBy()
        );

        // 4. Handle optimistic lock conflict
        if (rowsAffected == 0) {
            throw new OptimisticLockException(
                    "Promotion",
                    currentDomain.getId().toString(),
                    currentDomain.getVersion()
            );
        }

        // 5. Reload from DB to get the latest state (including new version)
        return loadDomain(currentDomain.getId());
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
