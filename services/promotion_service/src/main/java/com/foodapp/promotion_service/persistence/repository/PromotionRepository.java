package com.foodapp.promotion_service.persistence.repository;

import com.foodapp.promotion_service.persistence.entitty.PromotionEntity;

public interface PromotionRepository {
    PromotionEntity findById(String id);
    /**
     * Saves a new promotion entity to the database.
     * Corresponds to an <insert> tag in the XML.
     */
    void save(PromotionEntity entity);
}
