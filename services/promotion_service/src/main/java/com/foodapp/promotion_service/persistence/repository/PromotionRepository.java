package com.foodapp.promotion_service.persistence.repository;

import com.foodapp.promotion_service.persistence.entity.PromotionEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PromotionRepository {
    PromotionEntity findById(String id);

    List<PromotionEntity> findAll();

    void save(PromotionEntity entity);
}
