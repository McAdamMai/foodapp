package com.foodapp.promotion_service.domain.listener;

import com.foodapp.promotion_service.domain.event.PromotionChangedDomainEvent;
import com.foodapp.promotion_service.domain.service.OutboxEventEmitter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PromotionEventListener {

    private final OutboxEventEmitter outboxEventEmitter;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPromotionChanged(PromotionChangedDomainEvent event) {

        outboxEventEmitter.emitPromotionChangeIfNeeded(
                event.getOldPromotionDomain(),
                event.getNewPromotionDomain(),
                event.getCorrelationId()
        );
    }
}
