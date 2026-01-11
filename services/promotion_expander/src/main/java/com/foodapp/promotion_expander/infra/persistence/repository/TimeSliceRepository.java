package com.foodapp.promotion_expander.infra.persistence.repository;

import com.foodapp.promotion_expander.infra.persistence.entity.TimeSliceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface TimeSliceRepository {
    // should be break into two steps and being called by a transactional method, to secure atomicity
    //void replaceSlicesForPromotion(List<TimeSliceEntity> timeSlices)
    List<TimeSliceEntity> findByPromotionId(@Param("promotionId") UUID promotionId);
    void deleteSlicesByPromotionId(@Param("promotionId") UUID promotionId);
    void insertBatch(@Param("slices") List<TimeSliceEntity> entities);
    void updateContentByPromotionId(@Param("promotionId") UUID promotionId, @Param("updateParams") TimeSliceEntity updateParams);

}