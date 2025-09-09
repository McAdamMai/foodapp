package com.foodapp.price_reader.persistence.repository;

import com.foodapp.price_reader.domain.models.PriceKey;
import com.foodapp.price_reader.persistence.entity.PriceSnapshotIntervalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PriceSnapshotIntervalRepository{
    List<PriceSnapshotIntervalEntity> findOverlapping(
            PriceKey key,
            Instant from,
            Instant to,
            int limit
    );
}
