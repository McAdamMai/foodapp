package com.foodapp.promotion_service.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.promotion_service.api.controller.PromotionChangedEventPayload;
import com.foodapp.promotion_service.domain.mapper.PromotionMapper;
import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.domain.model.PromotionOutboxDomain;
import com.foodapp.promotion_service.domain.model.enums.EventType;
import com.foodapp.promotion_service.domain.model.enums.MaskType;
import com.foodapp.promotion_service.persistence.repository.PromotionOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionOutboxService {
    private final PromotionOutboxRepository repo;
    private final ObjectMapper mapper;

    public void saveOutbox(PromotionChangedEventPayload payload) {
        try {
            // storing the Full New State plus a Change Mask is the industry standard
            String json = mapper.writeValueAsString(payload);
            List<String> changeMask = payload.changeMask().stream()
                    .map(MaskType::name)
                    .toList();
            PromotionOutboxDomain newOutbox = PromotionOutboxDomain.createOutbox(
                    payload.messageId(),
                    payload.promotionId(),
                    payload.promotionVersion(),
                    changeMask,
                    json
            );

            repo.createOutbox(PromotionMapper.toEntity(newOutbox));

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload",e);
        }
    }

}
