package com.foodapp.price_reader.domain.service;

import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.mapper.PriceIntervalMapper;
import com.foodapp.price_reader.persistence.entity.PriceSnapshotIntervalEntity;
import com.foodapp.price_reader.persistence.repository.PriceSnapshotIntervalRepository;
import com.foodapp.price_reader.persistence.repository.jpa.JpaPriceSnapshotIntervalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminRestfulService {

    private final JpaPriceSnapshotIntervalRepository intervalRepo;
    private final PriceIntervalMapper mapper; // Domain <-> Entity 映射器

    /**
     * 保存或更新价格快照
     */
    public PriceInterval savePrice(PriceInterval domain) {
        // Domain -> Entity
        PriceSnapshotIntervalEntity entity = mapper.toEntity(domain);
        PriceSnapshotIntervalEntity saved = intervalRepo.save(entity);
        // Entity -> Domain
        return mapper.toDomain(saved);
    }

    /**
     * 根据 ID 查找价格快照
     */
    public Optional<PriceInterval> findById(String id) {
        return intervalRepo.findById(id)
                .map(mapper::toDomain);
    }

    /**
     * 查询所有价格快照
     */
    public List<PriceInterval> findAll() {
        return intervalRepo.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    public Optional<PriceInterval> findByValidPriceForInstant(String skuId, Instant at){
        return intervalRepo.findByValidPriceForInstant(skuId, at).map(mapper::toDomain);
    }
}

