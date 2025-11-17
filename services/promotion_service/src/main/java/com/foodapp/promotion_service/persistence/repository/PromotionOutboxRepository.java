package com.foodapp.promotion_service.persistence.repository;

import com.foodapp.promotion_service.persistence.entity.PromotionOutboxEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromotionOutboxRepository {
    void createOutbox(PromotionOutboxEntity entity);
}
