package com.foodapp.promotion_service.domain.listener;

import com.foodapp.promotion_service.domain.event.PromotionChangedDomainEvent;
import com.foodapp.promotion_service.domain.service.OutboxEventEmitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionEventListener {

    private final OutboxEventEmitter outboxEventEmitter;
    // transaction starts -> promotion save -> call listener -> outbox save -> commit
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onPromotionChanged(PromotionChangedDomainEvent event) {
        log.info("Trigger Promotion Event");
        outboxEventEmitter.emitPromotionChangeIfNeeded(
                event.getOldPromotionDomain(),
                event.getNewPromotionDomain()
        );
    }
}
