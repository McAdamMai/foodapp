package com.foodapp.base_price_manager.infrastructure.persistence.repository;

import com.foodapp.base_price_manager.infrastructure.persistence.entity.BasePriceChangeRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BasePriceChangeRequestJpaRepository extends JpaRepository<BasePriceChangeRequestEntity, Long> {
}
