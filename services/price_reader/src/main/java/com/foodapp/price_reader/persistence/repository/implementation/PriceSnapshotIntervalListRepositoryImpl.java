package com.foodapp.price_reader.persistence.repository.implementation;

import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.mapper.PriceIntervalMapper;
import com.foodapp.price_reader.persistence.entity.PriceSnapshotIntervalEntity;
import com.foodapp.price_reader.persistence.repository.PriceSnapshotIntervalListRepository;
import com.foodapp.price_reader.persistence.repository.jpa.JpaPriceSnapshotIntervalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PriceSnapshotIntervalListRepositoryImpl implements PriceSnapshotIntervalListRepository {
    private final JpaPriceSnapshotIntervalRepository repository;
    private final PriceIntervalMapper priceIntervalMapper;

    @Override
    public Map<String, Optional<PriceInterval>> getSnapshotPriceList(List<String> skuIds, Instant at){
        if (skuIds == null || skuIds.isEmpty()){
            return Collections.emptyMap();
        }
        List<PriceSnapshotIntervalEntity> validSnapshots = repository.findByValidPriceListForInstant(skuIds, at);
        Map<String, PriceSnapshotIntervalEntity> foundSnapshots = validSnapshots.stream()
                .collect(Collectors.toMap(
                        PriceSnapshotIntervalEntity::getSkuId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        return skuIds.stream()
                .collect(Collectors.toMap(
                        skuId -> skuId,
                        skuId -> Optional.ofNullable(foundSnapshots.get(skuId))
                                .map(priceIntervalMapper::toDomain) // only when entity is not empty
                ));
    }
}
