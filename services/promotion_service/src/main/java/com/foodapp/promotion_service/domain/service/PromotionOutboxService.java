package com.foodapp.promotion_service.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.promotion_service.domain.model.PromotionChangedEventPayload;
import com.foodapp.promotion_service.domain.mapper.PromotionMapper;
import com.foodapp.promotion_service.domain.model.PromotionOutboxDomain;
import com.foodapp.promotion_service.domain.model.enums.MaskType;
import com.foodapp.promotion_service.persistence.repository.PromotionOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionOutboxService { // use to store outbox data in a db
    private final PromotionOutboxRepository repo;
    private final ObjectMapper mapper;

    public void saveOutbox(PromotionChangedEventPayload payload) {
        List<String> changeMask = payload.changeMask().stream()
                    .map(MaskType::name)
                    .toList();
        PromotionOutboxDomain newOutbox = PromotionOutboxDomain.createOutbox(
                    payload.messageId(),
                    payload.promotionId(),
                    payload.promotionVersion(),
                    changeMask,
                    payload
        );
        repo.createOutbox(PromotionMapper.toEntity(newOutbox));
    }
}
