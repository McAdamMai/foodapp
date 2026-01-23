package com.foodapp.promotion_expander.infra.persistence.repository;

import com.foodapp.promotion_expander.infra.persistence.entity.ExpanderTrackerEntity;
import com.foodapp.promotion_expander.infra.persistence.entity.TimeSliceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Mapper
public interface ExpanderTrackerRepository {
    // Returns 1 if updated, 0 if the existing version was higher or equal
    int updateVersionIfNewer(ExpanderTrackerEntity expanderTrackerEntity);

    List<ExpanderTrackerEntity> findBatchNeedingExtension(@Param("targetHorizontal") LocalDate targetHorizontal,
                                                          @Param("rollingBatchSize") int rollingBatchSize);

    void updateCoveredUntil(@Param("promotionId")UUID promotionId, @Param("endDate") LocalDate endDate);
}
