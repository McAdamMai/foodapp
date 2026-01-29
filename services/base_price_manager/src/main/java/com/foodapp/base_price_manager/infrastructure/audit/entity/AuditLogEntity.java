package com.foodapp.base_price_manager.infrastructure.audit.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
@Entity
@Table (
        name = "audit_log",
        indexes = {
                @Index(name = "idx_audit_entity_id_created_at", columnList = "entity_id,created_at"),
                @Index(name = "idx_audit_created_at", columnList = "created_at")

        }

)

public class AuditLogEntity {

    // Keep this stable forever (don't tie it to Java class names).
    private static final String ENTITY_TYPE_BASE_PRICE_CHANGE_REQUEST = "BasePriceChangeRequest";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_id", nullable = false, updatable = false)
    private Long entityId;

    @Column(name = "entity_version", nullable = false, updatable = false)
    private Long entityVersion;

    @Column(name = "entity_type", nullable = false, updatable = false, length = 64)
    private String entityType;

    @Column(name = "action", nullable = false, updatable = false, length = 64)
    private String action;

    @Column(name = "actor", nullable = false, updatable = false, length = 128)
    private String actor;

    @Column(name = "role", updatable = false, length = 64)
    private String role;

    @Column(name = "fsm_event", updatable = false, length = 64)
    private String fsmEvent;

    @Lob
    @Column(name = "before_json", columnDefinition = "TEXT")
    private String beforeJson;

    @Lob
    @Column(name = "after_json", columnDefinition = "TEXT")
    private String afterJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // For JPA only
    protected AuditLogEntity() {}

    /**
     * Factory for BasePriceChangeRequest audit logs.
     * - Required: entityId, entityVersion, action, actor, createdAt
     * - Optional: role, fsmEvent, beforeJson, afterJson
     */
    public static AuditLogEntity createForBasePriceChangeRequest(
            Long entityId,
            Long entityVersion,
            String action,
            String actor,
            String role,
            String fsmEvent,
            String beforeJson,
            String afterJson,
            Instant createdAt
    ) {
        requireNonNull(entityId, "entityId");
        requireNonNull(entityVersion, "entityVersion");
        requireNonBlank(action, "action");
        requireNonBlank(actor, "actor");
        requireNonNull(createdAt, "createdAt");

        AuditLogEntity log = new AuditLogEntity();
        log.entityId = entityId;
        log.entityVersion = entityVersion;
        log.entityType = ENTITY_TYPE_BASE_PRICE_CHANGE_REQUEST;
        log.action = action;
        log.actor = actor;
        log.role = blankToNull(role);
        log.fsmEvent = blankToNull(fsmEvent);
        log.beforeJson = beforeJson;
        log.afterJson = afterJson;
        log.createdAt = createdAt;
        return log;
    }

    private static void requireNonNull(Object v, String name) {
        if (v == null) throw new IllegalArgumentException(name + " must not be null");
    }

    private static void requireNonBlank(String v, String name) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    private static String blankToNull(String v) {
        return (v == null || v.trim().isEmpty()) ? null : v;
    }

    // Optional but recommended for entity identity stability in sets/caches
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditLogEntity other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
