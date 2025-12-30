package com.foodapp.promotion_service.domain.service;

import com.foodapp.promotion_service.api.controller.PromotionChangedEventPayload;
import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.domain.model.PromotionRules;
import com.foodapp.promotion_service.domain.model.enums.MaskType;
import com.foodapp.promotion_service.fsm.PromotionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxEventEmitter {

    private final PromotionOutboxService promotionOutboxService;

    public void emitPromotionChangeIfNeeded(PromotionDomain oldPromotion, PromotionDomain newPromotion) {
        // 1. Gatekeeper: Only published promotions matter
        if (newPromotion.getStatus() != PromotionStatus.PUBLISHED) {
            return;
        }

        List<MaskType> changeMask = new ArrayList<>();

        // --- A. Status Change ---
        if (!Objects.equals(oldPromotion.getStatus(), PromotionStatus.PUBLISHED)) {
            changeMask.add(MaskType.STATUS);
        }

        // --- B. Date Change (Heavy) ---
        if (!Objects.equals(oldPromotion.getStartDate(), newPromotion.getStartDate())
                || !Objects.equals(oldPromotion.getEndDate(), newPromotion.getEndDate())) {
            changeMask.add(MaskType.DATES);
        }
        // --- C. Deep Rule Inspection ---
        PromotionRules oldRules = oldPromotion.getJsonRules();
        PromotionRules newRules = newPromotion.getJsonRules();
        if (oldRules != null && newRules != null) {
            // Schedule Changes (Recurrence, Time Windows) -> EXPANDER
            if(!Objects.equals(oldRules.getScheduleRules(), newRules.getScheduleRules())) {
                changeMask.add(MaskType.SCHEDULE);
            }

            // Stacking/Priority Changes -> EXPANDER
            if(!Objects.equals(oldRules.getStackingRules(), newRules.getStackingRules())) {
                changeMask.add(MaskType.PRIORITY);
            }

            // Effect/Price Changes -> WRITER
            if(!Objects.equals(oldRules.getEffect(), newRules.getEffect())) {
                changeMask.add(MaskType.EFFECT);
            }
        }else if (newRules != null) {
            // New added rules
            changeMask.add(MaskType.SCHEDULE);
            changeMask.add(MaskType.EFFECT);
        }

        // 3. save to outbox
        if (!changeMask.isEmpty()) {
            PromotionChangedEventPayload payload = new PromotionChangedEventPayload(
                    UUID.randomUUID(),
                    newPromotion.getId(),
                    newPromotion.getVersion(),
                    newPromotion.getStatus().name(),
                    changeMask,
                    newPromotion.getStartDate(),
                    newPromotion.getEndDate(),
                    newPromotion.getTemplateId(),

                    newPromotion.getJsonRules()
            );
            promotionOutboxService.saveOutbox(payload);
        }
    }
}
