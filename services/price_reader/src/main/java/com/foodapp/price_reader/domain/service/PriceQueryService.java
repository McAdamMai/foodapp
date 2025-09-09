package com.foodapp.price_reader.domain.service;

import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.mapper.PriceIntervalMapper;
import com.foodapp.price_reader.persistence.repository.jpa.JpaPriceSnapshotIntervalRepository;
import lombok.RequiredArgsConstructor;
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
    public Optional<PriceInterval> findPrice(String skuID, String currency, Instant at) {
        return repo.findByValidPriceForInstant(skuID, at)
                .map(domainMapper::toDomain);
    }
}
