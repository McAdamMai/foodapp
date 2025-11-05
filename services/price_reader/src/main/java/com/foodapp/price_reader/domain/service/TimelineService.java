package com.foodapp.price_reader.domain.service;

import com.foodapp.price_reader.cache.RedisCacheConfig;
import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.domain.models.PriceKey;
import com.foodapp.price_reader.enums.LimitConstraints;
import com.foodapp.price_reader.mapper.PriceIntervalMapper;
import com.foodapp.price_reader.persistence.entity.PriceSnapshotIntervalEntity;
import com.foodapp.price_reader.persistence.repository.PriceSnapshotIntervalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimelineService {
    private final PriceSnapshotIntervalRepository repo;
    private final PriceIntervalMapper mapper;

    // static is used to store constants;
    private static final int MAX_LIMIT = LimitConstraints.MAX_LIMIT.getValue();
    private static final int MIN_LIMIT = LimitConstraints.MIN_LIMIT.getValue();

    @Cacheable(
            value = RedisCacheConfig.TIMELINE_CACHE,
            key = "#key.tenantId() + ':' + #key.storeId() + ':' + #key.skuId() + '::' + #from.toEpochMilli() + '::' + #to.toEpochMilli()  + '::' + #limit"
    )
    public List<PriceInterval> getTimeline(PriceKey key, Instant from, Instant to, int limit){
        Objects.requireNonNull(key);
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        if(!from.isBefore(to)){
            throw new IllegalArgumentException("from must be before to");
        }
        // min =< limit =< max
        int clampedLimit = Math.min(Math.max(MIN_LIMIT, limit), MAX_LIMIT);

        //Defensive re-clamping
        if (limit != clampedLimit){
            throw new IllegalArgumentException(
                    String.format("Requested limit %d was clamped to %d", limit, clampedLimit)
            );
        }

        // using composite key to lookup data
        List<PriceSnapshotIntervalEntity> entities = repo.findOverlapping(key, from, to, clampedLimit);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Cacheable(
            value = RedisCacheConfig.TIMELINE_CACHE,
            key = "'batch::' + #keys.hashCode() + '::' + #from.toEpochMilli() + '::' + #to.toEpochMilli() + '::' + #limit"
    )
    public Map<PriceKey, List<PriceInterval>> getTimelinesBatch(
            List<PriceKey> keys,
            Instant from,
            Instant to,
            int limit
    ){
        Objects.requireNonNull(keys,"keys must not be null");
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        if (keys.isEmpty()){
            return Collections.emptyMap();
        }

        if (!from.isBefore(to)){
            throw new IllegalArgumentException("from must before to");
        }

        int clampedLimit = Math.min(Math.max(MIN_LIMIT, limit), MAX_LIMIT);

        //Searching the timeline for every key
        Map<PriceKey, List<PriceInterval>> result = new HashMap<>();
        for (PriceKey key : keys){
            List<PriceSnapshotIntervalEntity> entities = repo.findOverlapping(key,from, to, clampedLimit);
            List<PriceInterval> intervals = entities.stream().map(mapper::toDomain).collect(Collectors.toList());
            result.put(key, intervals);
        }
        return result;
    }
}
