package com.foodapp.promotion_service.persistence.repository;

import com.foodapp.promotion_service.persistence.entitty.PromotionEntity;

import java.util.List;

public interface PromotionRepository {
    PromotionEntity findById(String id);

    List<PromotionEntity> findAll();

    void save(PromotionEntity entity);
}
