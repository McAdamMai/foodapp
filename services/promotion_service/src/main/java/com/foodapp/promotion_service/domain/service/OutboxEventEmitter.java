package com.foodapp.promotion_service.domain.service;

import com.foodapp.promotion_service.api.controller.PromotionChangedEventPayload;
import com.foodapp.promotion_service.domain.model.PromotionDomain;
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
        // 1. gatekeeper: decline any non-published promotion
        if (newPromotion.getStatus() != PromotionStatus.PUBLISHED) {
            return;
        }

        List<MaskType> changeMask = new ArrayList<>();

        // 2. detect changes, only new promotion will go through this change
        if (!Objects.equals(oldPromotion.getStatus(), PromotionStatus.PUBLISHED)) {
            changeMask.add(MaskType.STATUS);
        }

        // check for date changes -- modified promotion
        if (!Objects.equals(oldPromotion.getStartDate(), newPromotion.getStartDate())
                || !Objects.equals(oldPromotion.getEndDate(), newPromotion.getEndDate())) {
            changeMask.add(MaskType.DATEs);
        }

        // check for rule/template changes -- modified promotion
        if (!Objects.equals(oldPromotion.getTemplateId(), newPromotion.getTemplateId())) {
            changeMask.add(MaskType.RULES);
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
                    newPromotion.getTemplateId()
            );
            promotionOutboxService.saveOutbox(payload);
        }
    }
}
