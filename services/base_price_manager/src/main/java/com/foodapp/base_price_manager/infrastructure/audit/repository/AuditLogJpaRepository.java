package com.foodapp.base_price_manager.infrastructure.audit.repository;

import com.foodapp.base_price_manager.infrastructure.audit.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long> {
}
