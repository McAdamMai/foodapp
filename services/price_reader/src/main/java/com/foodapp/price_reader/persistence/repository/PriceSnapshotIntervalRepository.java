package com.foodapp.price_reader.persistence.repository;

import com.foodapp.price_reader.persistence.entity.MerchandisePrice;
import com.foodapp.price_reader.persistence.entity.PriceSnapshotIntervalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PriceSnapshotIntervalRepository extends JpaRepository<PriceSnapshotIntervalEntity, String> {

    @Query("SELECT ps FROM PriceSnapshotIntervalEntity ps " +
            "WHERE ps.skuId = :skuId " +
            "AND ps.startAtUtc <= :atInstant " +
            "AND (ps.endAtUtc > :atInstant OR ps.endAtUtc IS NULL)")
    Optional<PriceSnapshotIntervalEntity> findByValidPriceForInstant(
            @Param("skuId") String skuId,
            @Param("atInstant") Instant atInstant
    );
}
