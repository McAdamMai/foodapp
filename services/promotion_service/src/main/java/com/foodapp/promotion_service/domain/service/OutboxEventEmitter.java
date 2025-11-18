package com.foodapp.promotion_service.domain.service;

import com.foodapp.promotion_service.api.controller.PromotionChangedEventPayload;
import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.domain.model.enums.MaskType;
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

    public void emitPromotionChangeIfNeeded(PromotionDomain oldPromotion, PromotionDomain newPromotion, UUID correlationId) {
        List<MaskType> changeMask = new ArrayList<>();

        if (!Objects.equals(oldPromotion.getStartDate(), newPromotion.getStartDate())
                || !Objects.equals(oldPromotion.getEndDate(), newPromotion.getEndDate())) {
            changeMask.add(MaskType.DATEs);
        }

        if (!Objects.equals(oldPromotion.getTemplateId(), newPromotion.getTemplateId())){
            changeMask.add(MaskType.RULES);
        }

        if (!Objects.equals(oldPromotion.getName(), newPromotion.getName())
                ||!Objects.equals(oldPromotion.getDescription(), newPromotion.getDescription())) {
            changeMask.add(MaskType.META);
        }

        if (!Objects.equals(oldPromotion.getStatus(), newPromotion.getStatus())) {
            changeMask.add(MaskType.STATUS);
        }

        // create payload for database
        if (!changeMask.isEmpty()) {
            PromotionChangedEventPayload payload = new PromotionChangedEventPayload(
                    UUID.randomUUID(),
                    newPromotion.getId(),
                    newPromotion.getVersion(),
                    newPromotion.getStatus().name(),
                    changeMask,
                    Instant.now(),
                    correlationId
            );
            promotionOutboxService.saveOutbox(payload);
        }
    }
}
