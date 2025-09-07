package com.foodapp.price_reader.domain.service;

import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.mapper.PriceIntervalMapper;
import com.foodapp.price_reader.persistence.entity.MerchandisePrice;
import com.foodapp.price_reader.persistence.repository.MerchandisePriceRepository;
import com.foodapp.price_reader.persistence.repository.PriceSnapshotIntervalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PriceQueryService {

    private final PriceSnapshotIntervalRepository repo;

    private final PriceIntervalMapper domainMapper;
    //https://poe.com/s/dKxtsOkluHPXl6jONJI7 following this to implement lookup

    //Avoid transport types (gRPC Timestamp) in the domain/service layer.
    public Optional<PriceInterval> findPrice(String skuID, String currency, Instant at) {
        return repo.findByValidPriceForInstant(skuID, at)
                .map(domainMapper::toDomain);
    }

    // Method for restful



}
