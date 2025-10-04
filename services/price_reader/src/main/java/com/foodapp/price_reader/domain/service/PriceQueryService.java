package com.foodapp.price_reader.domain.service;

import com.foodapp.price_reader.cache.RedisCacheConfig;
import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.mapper.PriceIntervalMapper;
import com.foodapp.price_reader.persistence.repository.jpa.JpaPriceSnapshotIntervalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PriceQueryService {

    private final JpaPriceSnapshotIntervalRepository repo;

    private final PriceIntervalMapper domainMapper;
    //https://poe.com/s/dKxtsOkluHPXl6jONJI7 following this to implement lookup

    //Avoid transport types (gRPC Timestamp) in the domain/service layer.
    // Express absence explicitly, good fit for returning types where "not found is normal"
    @Cacheable(
            value = RedisCacheConfig.PRICE_CACHE,
            key = "#skuID + '::' + #at.toEpochMilli()",
           unless = "#result == null || #result instanceof T(java.util.Optional) && !#result.isPresent()" // differentiate the null and not found
    )
    public Optional<PriceInterval> getPrice(String skuID, Instant at) {
        return repo.findByValidPriceForInstant(skuID, at)
                .map(domainMapper::toDomain);
    }
}
