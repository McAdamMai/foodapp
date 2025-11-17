package com.foodapp.promotion_service.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.promotion_service.api.controller.PromotionChangedEventPayload;
import com.foodapp.promotion_service.domain.mapper.PromotionMapper;
import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.domain.model.PromotionOutboxDomain;
import com.foodapp.promotion_service.persistence.repository.PromotionOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromotionOutboxService {
    private final PromotionOutboxRepository repo;
    private final ObjectMapper mapper;

    public void saveOutbox(PromotionChangedEventPayload payload) {
        try {
            String json = mapper.writeValueAsString(payload);

            PromotionOutboxDomain newOutbox = PromotionOutboxDomain.createOutbox(
                    payload.messageId(),
                    payload.promotionId(),
                    payload.promotionVersion(),
                    payload.changeMask(),
                    json,
                    payload.occurredAt()
            );

            repo.createOutbox(PromotionMapper.toEntity(newOutbox));

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload",e);
        }
    }
}
