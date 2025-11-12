package com.foodapp.promotion_service.domain.exception;

/**
 * Thrown when an entity update fails due to optimistic locking conflict.
 * This indicates that the entity was modified by another user/process
 * between the read and update operations.
 */
public class OptimisticLockException extends RuntimeException {

    private final String entityType;
    private final String entityId;
    private final Integer expectedVersion;

    public OptimisticLockException(String message) {
        super(message);
        this.entityType = null;
        this.entityId = null;
        this.expectedVersion = null;
    }

    public OptimisticLockException(String entityType, String entityId, Integer expectedVersion) {
        super(String.format(
                "%s with ID '%s' was modified by another user (expected version: %d)",
                entityType, entityId, expectedVersion
        ));
        this.entityType = entityType;
        this.entityId = entityId;
        this.expectedVersion = expectedVersion;
    }

    public OptimisticLockException(String message, Throwable cause) {
        super(message, cause);
        this.entityType = null;
        this.entityId = null;
        this.expectedVersion = null;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public Integer getExpectedVersion() {
        return expectedVersion;
    }
}