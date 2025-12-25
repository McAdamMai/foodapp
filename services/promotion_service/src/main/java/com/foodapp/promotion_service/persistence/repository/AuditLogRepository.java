package com.foodapp.promotion_service.persistence.repository;

import com.foodapp.promotion_service.persistence.entity.AuditLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogRepository {
    int insert(AuditLogEntity entity);
}
