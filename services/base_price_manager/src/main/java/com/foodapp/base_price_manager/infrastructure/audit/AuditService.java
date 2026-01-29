package com.foodapp.base_price_manager.infrastructure.audit;

import com.foodapp.base_price_manager.infrastructure.audit.entity.AuditLogEntity;
import com.foodapp.base_price_manager.infrastructure.audit.repository.AuditLogJpaRepository;
import com.foodapp.base_price_manager.common.time.ClockProvider;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogJpaRepository auditRepo;
    private final ClockProvider clock;

    public AuditService(AuditLogJpaRepository auditRepo, ClockProvider clock){
        this.auditRepo = auditRepo;
        this.clock = clock;
    }

    public void recordBasePriceChangeRequest(
            Long entityId,
            Long entityVersion,
            String action,
            String actor,
            String role,
            String fsmEvent,
            String beforeJson,
            String afterJson

    ){
        AuditLogEntity log = AuditLogEntity.createForBasePriceChangeRequest(
                entityId,
                entityVersion,
                action,
                actor,
                role,
                fsmEvent,
                beforeJson,
                afterJson,
                clock.nowInstant()
        );
        auditRepo.save(log);

    }
}