package com.foodapp.promotion_service.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.promotion_service.domain.event.PromotionChangedDomainEvent;
import com.foodapp.promotion_service.persistence.entity.AuditLogEntity;
import com.foodapp.promotion_service.persistence.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Persists audit logs for domain events.
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void savePromotionAudit(PromotionChangedDomainEvent event) {
        // Defensive checks: audit requires actor/action and a target entity
        if (event == null || event.getNewPromotionDomain() == null) {
            return;
        }
        if (event.getActor() == null || event.getAction() == null) {
            return;
        }

        String beforeJson = toJsonSafely(event.getOldPromotionDomain());
        String afterJson = toJsonSafely(event.getNewPromotionDomain());

        // Compute change mask (best-effort)
        String changeMaskJson = toJsonSafely(computePromotionChangeMask(event));

        AuditLogEntity entity = AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .entityType("PROMOTION")
                .entityId(event.getNewPromotionDomain().getId())
                .action(event.getAction().name())
                .actor(event.getActor())
                .role(event.getRole() == null ? null : event.getRole().name())
                .entityVersion(event.getNewPromotionDomain().getVersion())
                .fsmEvent(event.getEvent() == null ? null : event.getEvent().name())
                .changeMask(changeMaskJson)
                .beforeJson(beforeJson)
                .afterJson(afterJson)
                .createdAt(Instant.now())
                .build();

        auditLogRepository.insert(entity);
    }

    /**
     * Computes a best-effort change mask for audit readability.
     * This does not replace outbox change mask; it's for audit.
     */
    private List<String> computePromotionChangeMask(PromotionChangedDomainEvent event) {
        var oldP = event.getOldPromotionDomain();
        var newP = event.getNewPromotionDomain();

        List<String> mask = new ArrayList<>();
        if (oldP == null || newP == null) {
            return mask;
        }

        // Status changes
        if (!Objects.equals(oldP.getStatus(), newP.getStatus())) {
            mask.add("STATUS");
        }

        // Date changes
        if (!Objects.equals(oldP.getStartDate(), newP.getStartDate())
                || !Objects.equals(oldP.getEndDate(), newP.getEndDate())) {
            mask.add("DATES");
        }

        // Template/rules changes
        if (!Objects.equals(oldP.getTemplateId(), newP.getTemplateId())) {
            mask.add("RULES");
        }

        // Basic fields changes (optional but useful)
        if (!Objects.equals(oldP.getName(), newP.getName())) {
            mask.add("NAME");
        }
        if (!Objects.equals(oldP.getDescription(), newP.getDescription())) {
            mask.add("DESCRIPTION");
        }

        return mask;
    }

    private String toJsonSafely(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            // Fallback to avoid blocking business flow due to audit serialization issues
            return "{\"error\":\"failed_to_serialize\"}";
        }
    }
}
