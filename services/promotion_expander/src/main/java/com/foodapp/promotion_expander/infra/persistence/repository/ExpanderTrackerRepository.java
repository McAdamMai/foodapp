package com.foodapp.promotion_expander.infra.persistence.repository;

import com.foodapp.promotion_expander.infra.persistence.entity.TimeSliceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface ExpanderTrackerRepository {
    // Returns 1 if updated, 0 if the existing version was higher or equal
    int updateVersionIfNewer(@Param("promotionId") UUID promotionId, @Param("newVersion") Integer newVersion);
}
