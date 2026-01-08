package com.foodapp.promotion_service.persistence.repository;

import com.foodapp.promotion_service.fsm.enums.PromotionStatus;
import com.foodapp.promotion_service.persistence.entity.PromotionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface PromotionRepository {
    //must explicitly name the parameter in your Java interface using @Param. This tells MyBatis, "Treat this argument as the variable named 'id'."
    PromotionEntity findById(@Param("id") UUID id);

    List<PromotionEntity> findAll();

    void save(PromotionEntity entity);

    int updatePromotionDetails(PromotionEntity entity);

    int updateStateTransition(
            @Param("id") UUID id,
            @Param("status") PromotionStatus status,
            @Param("expectedStatus") PromotionStatus expectedStatus,
            @Param("version") Integer version,
            @Param("reviewedBy") String reviewedBy,
            @Param("publishedBy") String publishedBy
    );
}
