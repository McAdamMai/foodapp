package com.foodapp.price_reader.persistence.repository.implementation;

import com.foodapp.price_reader.domain.models.PriceKey;
import com.foodapp.price_reader.persistence.entity.PriceSnapshotIntervalEntity;
import com.foodapp.price_reader.persistence.repository.PriceSnapshotIntervalRepository;
import com.foodapp.price_reader.persistence.repository.jpa.JpaPriceSnapshotIntervalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PriceSnapshotIntervalRepositoryImpl implements PriceSnapshotIntervalRepository {

    private final JpaPriceSnapshotIntervalRepository jpaRepo;

    @Override
    public List<PriceSnapshotIntervalEntity> findOverlapping(PriceKey key, Instant from, Instant to, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("startAtUtc").ascending());
        return jpaRepo.findOverlapping(
                key.tenantId(),
                key.storeId(),
                key.skuId(),
                key.userSegId(),
                key.channelId(),
                from,
                to,
                pageable
        );
    }
}
