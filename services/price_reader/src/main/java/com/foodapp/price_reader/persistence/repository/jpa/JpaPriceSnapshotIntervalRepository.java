package com.foodapp.price_reader.persistence.repository.jpa;

import com.foodapp.price_reader.persistence.entity.PriceSnapshotIntervalEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaPriceSnapshotIntervalRepository extends JpaRepository<PriceSnapshotIntervalEntity, String> {

    /**
     * Find a price snapshot that is valid for a specific SKU ID and instant.
     */
    @Query("SELECT ps FROM PriceSnapshotIntervalEntity ps " +
            "WHERE ps.skuId = :skuId " +
            "AND ps.startAtUtc <= :atInstant " +
            "AND (ps.endAtUtc > :atInstant OR ps.endAtUtc IS NULL)")
    Optional<PriceSnapshotIntervalEntity> findByValidPriceForInstant(
            @Param("skuId") String skuId,
            @Param("atInstant") Instant atInstant
    );

    /**
     * Find overlapping price snapshots based on multiple attributes of the PriceKey.
     */
    @Query("SELECT ps FROM PriceSnapshotIntervalEntity ps " +
            "WHERE ps.tenantId = :tenantId " +
            "AND ps.storeId = :storeId " +
            "AND ps.skuId = :skuId " +
            "AND (:userSegId IS NULL OR ps.userSegId = :userSegId) " +
            "AND (:channelId IS NULL OR ps.channelId = :channelId) " +
            "AND ps.startAtUtc < :to " +
            "AND (ps.endAtUtc > :from OR ps.endAtUtc IS NULL) " +
            "ORDER BY ps.startAtUtc ASC")
    List<PriceSnapshotIntervalEntity> findOverlapping(
            @Param("tenantId") String tenantId,
            @Param("storeId") String storeId,
            @Param("skuId") String skuId,
            @Param("userSegId") String userSegId,
            @Param("channelId") String channelId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );
}