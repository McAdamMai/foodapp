package com.foodapp.promotion_service.persistence.entity;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;
@Builder
@Data
public class AuditLogEntity {

    private UUID id;
    private String entityType;
    private UUID entityId;
    private String action;
    private String actor;
    private String role;
    private Integer entityVersion;
    private String fsmEvent;
    private String changeMask;   // store JSON array as String for now
    private String beforeJson;   // JSON string
    private String afterJson;    // JSON string
    private Instant createdAt;



}
