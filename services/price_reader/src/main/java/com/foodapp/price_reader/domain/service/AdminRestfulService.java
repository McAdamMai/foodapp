package com.foodapp.price_reader.domain.service;

import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.domain.models.PriceKey;
import com.foodapp.price_reader.mapper.PriceIntervalMapper;
import com.foodapp.price_reader.persistence.entity.PriceSnapshotIntervalEntity;
import com.foodapp.price_reader.persistence.repository.PriceSnapshotIntervalRepository;
import com.foodapp.price_reader.persistence.repository.jpa.JpaPriceSnapshotIntervalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminRestfulService {

    private final JpaPriceSnapshotIntervalRepository intervalRepo;
    private final PriceIntervalMapper mapper; // Domain <-> Entity
    private final PriceSnapshotIntervalRepository repo;


    public PriceInterval savePrice(PriceInterval domain) {
        // Domain -> Entity
        PriceSnapshotIntervalEntity entity = mapper.toEntity(domain);
        PriceSnapshotIntervalEntity saved = intervalRepo.save(entity);
        // Entity -> Domain
        return mapper.toDomain(saved);
    }

    public Optional<PriceInterval> findById(String id) {
        return intervalRepo.findById(id)
                .map(mapper::toDomain);
    }

    public List<PriceInterval> findAll() {
        return intervalRepo.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    public Optional<PriceInterval> findPrice(String skuId,Instant at){
        return intervalRepo.findByValidPriceForInstant(skuId, at).map(mapper::toDomain);
    }

    public List<PriceInterval> getTimeline(PriceKey key, Instant from, Instant to, int limit){
        Objects.requireNonNull(key);
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        if(!from.isBefore(to)){
            throw new IllegalArgumentException("from must be before to");
        }
        // using composite key to lookup data
        List<PriceSnapshotIntervalEntity> entities = repo.findOverlapping(key, from, to, limit);
        return entities.stream().map(mapper::toDomain).toList();
    }


}

