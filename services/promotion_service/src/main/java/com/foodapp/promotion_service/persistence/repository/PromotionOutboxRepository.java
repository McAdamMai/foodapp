package com.foodapp.promotion_service.persistence.repository;

import com.foodapp.promotion_service.persistence.entity.PromotionOutboxEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Mapper
public interface PromotionOutboxRepository {
    void createOutbox(PromotionOutboxEntity entity);

    List<PromotionOutboxEntity> findPendingBatch(@Param("batchSize") int batchSize);

    void markAsPublished(@Param("id")UUID id, @Param("now")OffsetDateTime now);
}


