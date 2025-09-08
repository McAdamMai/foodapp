package com.foodapp.price_reader.domain.service;

import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.domain.models.PriceKey;
import com.foodapp.price_reader.mapper.PriceIntervalMapper;
import com.foodapp.price_reader.persistence.entity.PriceSnapshotIntervalEntity;
import com.foodapp.price_reader.persistence.repository.PriceSnapshotIntervalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TimelineService {
    private final PriceSnapshotIntervalRepository repo;
    private final PriceIntervalMapper mapper;

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
