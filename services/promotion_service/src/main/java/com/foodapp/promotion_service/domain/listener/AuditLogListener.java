package com.foodapp.promotion_service.domain.listener;

import com.foodapp.promotion_service.domain.event.PromotionChangedDomainEvent;
import com.foodapp.promotion_service.domain.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens to domain events and persists audit logs.
 */
@Component
@RequiredArgsConstructor
public class AuditLogListener {

    private final AuditLogService auditLogService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPromotionChanged(PromotionChangedDomainEvent event) {
        auditLogService.savePromotionAudit(event);
    }
}
