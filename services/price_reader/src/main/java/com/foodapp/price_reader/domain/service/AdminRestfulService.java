package com.foodapp.price_reader.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.price_reader.adapters.api.dto.PriceIntervalDto;
import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.domain.models.PriceKey;
import com.foodapp.price_reader.mapper.PriceIntervalMapper;
import com.foodapp.price_reader.persistence.entity.PriceSnapshotIntervalEntity;
import com.foodapp.price_reader.persistence.repository.PriceSnapshotIntervalRepository;
import com.foodapp.price_reader.persistence.repository.jpa.JpaPriceSnapshotIntervalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.ls.LSException;

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
    private final ObjectMapper objectMapper;


    public List<PriceInterval> saveBatchPrices(List<PriceInterval> intervals) {
        if (intervals.size() > 100) {
            throw new IllegalArgumentException("Batch size cannot exceed 100");
        }
        List<PriceSnapshotIntervalEntity> entities = intervals.stream()
                .peek(this::validateBussinessRules)
                .map(mapper::toEntity)
                .toList();

        List<PriceSnapshotIntervalEntity> savedEntites =intervalRepo.saveAll(entities);

        return savedEntites.stream()
                .map(mapper::toDomain)
                .toList();




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
    public void validateBussinessRules(PriceInterval interval) {
        if (interval.startAtUtc().isAfter(interval.endAtUtc())) {
            throw new IllegalArgumentException("Start time must be earlier than end time");
        }
        if (interval.effectivePriceCent() > interval.priceComponent().get("regularPrice").asInt()) {
            throw new IllegalArgumentException("Effective price must not exceed regular price");
        }

        if (interval.effectivePriceCent() <= 0 || interval.priceComponent().get("regularPrice").asInt() <= 0) {
            throw new IllegalArgumentException("Prices must be greater than zero");
        }

        if (interval.priceComponent().get("taxRate").asInt() < 0) {
            throw new IllegalArgumentException("Tax rate must be >= 0");
        }
    }

    public void deleteBySkuId(String skuId){
        intervalRepo.deleteBySkuId(skuId);
    }

    @Transactional
    public void updateInterval(String id, PriceIntervalDto dto){
        try {
            int updated = intervalRepo.updateDynamicFields(
                    id,
                    dto.effectivePriceCent(),
                    dto.currency(),
                    dto.endAtUtc() != null ? Instant.parse(dto.endAtUtc()) : null,
                    dto.priceComponent() != null ? objectMapper.writeValueAsString(dto.priceComponent()) : null,
                    dto.provenance() != null ? objectMapper.writeValueAsString(dto.provenance()) : null
            );

            if (updated == 0) {
                throw new IllegalArgumentException("Record not found or unchanged: " + id);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON fields", e);
        }

    }




}
