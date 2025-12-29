package com.foodapp.promotion_service.domain.service;

import com.foodapp.promotion_service.api.controller.dto.request.PromotionUpdateRequest;
import com.foodapp.promotion_service.domain.exception.OptimisticLockException;
import com.foodapp.promotion_service.domain.exception.ResourceNotFoundException;
import com.foodapp.promotion_service.domain.model.PromotionRules;
import com.foodapp.promotion_service.persistence.entity.DayTemplateEntity;
import com.foodapp.promotion_service.persistence.repository.DayTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher; // <--- Add this import
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
import com.foodapp.promotion_service.domain.model.enums.AuditAction;


import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {
    private final PromotionStateMachine promotionStateMachine;
    private final PromotionRepository promotionRepository;
    private final UserAuthorizationService authService;
    private final DayTemplateRepository templateRepo;

    // Spring provides this automatically via Dependency Injection
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PromotionDomain create(
            String name,
            String description,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String createdBy,
            UUID templateId,
            PromotionRules overrideRules
    ) {
        PromotionRules finalRules;
        if (overrideRules != null) {
            // CaseA: user tweaked the rules
            finalRules = overrideRules;
        }else{
            DayTemplateEntity templateEntity = templateRepo.findById(templateId)
                    .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));
            finalRules = templateEntity.getRuleJson();
        }
        log.info("Rules set to : finalRules={}", finalRules);
        // add log for "log.info("Creating new promotion: name={}, createdBy={}", name, createdBy)"
        PromotionDomain newPromotion = PromotionDomain.createNew(
                name, description, startDate, endDate, createdBy, templateId, finalRules);
        // no mappers are required because toEntity is set static (stateless)
        // static methods = shared tools, while instance methods = behavior tied to data.
        // instance belong to each object
        PromotionEntity entityToSave = PromotionMapper.toEntity(newPromotion);
        promotionRepository.save(entityToSave);

        // LOGGING STEP 2
        log.info("Promotion created successfully: id={}", newPromotion.getId());
        return newPromotion;
    }

    // only the publisher can do urgent update
    @Transactional
    public PromotionDomain updateDetails(PromotionUpdateRequest request){
        // validation: must have something to update
        if (!request.hasUpdates()){
            throw new IllegalArgumentException("No fields to update");
        }

        authService.validateUserRole(request.updatedBy(), UserRole.PUBLISHER);

        // 1. Load current state
        PromotionDomain domain = loadDomain(request.id());

        // 2. Validate (read-only)
        domain.validateCanBeUpdated(request.updatedBy());

        // 3. Build update entity with CURRENT version
        PromotionEntity updateEntity = buildUpdateEntity(request, domain.getVersion());

        // 4. Execute atomic update with optimistic locking
        int rowAffected = promotionRepository.updatePromotionDetails(updateEntity);

        // 5. Handle optimistic lock conflict
        if (rowAffected == 0) {
            throw new OptimisticLockException("Promotion was modified by another user or is not editable");
        }

        // 6. Reload and return updated state
        PromotionDomain newDomain = loadDomain(request.id());

        eventPublisher.publishEvent(new com.foodapp.promotion_service.domain.event.PromotionChangedDomainEvent(
                domain,
                newDomain,
                request.updatedBy(),  // actor (userId)
                null,                 // role (not available in this flow)
                AuditAction.PROMOTION_UPDATE_DETAILS,  //business action
                null                                   // FSM event (not applicable)
        ));

        return newDomain;
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
        // 1. Validate actor role
        authService.validateUserRole(reviewedBy, UserRole.REVIEWER);

        // 2. load current state
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
        // 1. Validate actor role
        authService.validateUserRole(reviewedBy, UserRole.REVIEWER);

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

        // 1. Validate actor role
        authService.validateUserRole(publishedBy, UserRole.PUBLISHER);
        // 2. fetch current status
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
                currentDomain.getId(),
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

        // 5. Publish the event so the Listener works
        eventPublisher.publishEvent(new com.foodapp.promotion_service.domain.event.PromotionChangedDomainEvent(
                currentDomain,
                updatedDomain,
                actor,                     // userId
                userRole,                  // role
                mapAuditAction(event),     // business audit action
                event                      // FSM event


        ));

        // 6. Reload from DB to get the latest state (including new version)
        return loadDomain(currentDomain.getId());
    }

    private PromotionDomain loadDomain(UUID id) {
        return PromotionMapper.toDomain(
                promotionRepository.findById(id)
        );
    }

    private PromotionEntity buildUpdateEntity (
            PromotionUpdateRequest request,
            Integer currentVersion
    ) {
        PromotionEntity.PromotionEntityBuilder builder = PromotionEntity.builder()
                // FIX 1: Change getId() to id() (record accessor)
                .id(request.id())
                .version(currentVersion);

        // set only non-null fields
        if (request.name() != null) { // FIX 2: Change getName() to name()
            builder.name(request.name());
        }
        if (request.description() != null) { // FIX 3: Change getDescription() to description()
            builder.description(request.description());
        }
        if (request.startDate() != null) { // FIX 4: Change getStartDate() to startDate()
            builder.startDate(request.startDate());
        }
        if (request.endDate() != null) { // FIX 5: Change getEndDate() to endDate()
            builder.endDate(request.endDate());
        }
        if (request.templateId() != null) { // FIX 6: Change getTemplateId() to templateId()
            // The method signature uses UUID, and the accessor returns UUID. No casting needed.
            builder.templateId(request.templateId());
        }

        return builder.build();
    }

    /**
     * Maps FSM events to high-level audit actions.
     */
    private AuditAction mapAuditAction(PromotionEvent event) {
        return switch (event) {
            case SUBMIT -> AuditAction.PROMOTION_SUBMIT;
            case APPROVE -> AuditAction.PROMOTION_APPROVE;
            case REJECT -> AuditAction.PROMOTION_REJECT;
            case PUBLISH -> AuditAction.PROMOTION_PUBLISH;
            case ROLLBACK -> AuditAction.PROMOTION_ROLLBACK;
            case EDIT -> AuditAction.PROMOTION_UPDATE_DETAILS;
        };
    }
}
