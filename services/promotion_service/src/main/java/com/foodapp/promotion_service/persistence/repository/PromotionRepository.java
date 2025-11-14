package com.foodapp.promotion_service.persistence.repository;

import com.foodapp.promotion_service.fsm.PromotionStatus;
import com.foodapp.promotion_service.persistence.entity.PromotionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PromotionRepository {
    PromotionEntity findById(String id);

    List<PromotionEntity> findAll();

    void save(PromotionEntity entity);

    int updatePromotionDetails(PromotionEntity entity);

    int updateStateTransition(
            @Param("id") String id,
            @Param("status") PromotionStatus status,
            @Param("expectedStatus") PromotionStatus expectedStatus,
            @Param("version") Integer version,
            @Param("reviewedBy") String reviewedBy,
            @Param("publishedBy") String publishedBy
    );
}
