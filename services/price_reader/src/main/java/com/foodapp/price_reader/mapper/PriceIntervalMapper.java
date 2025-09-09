package com.foodapp.price_reader.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.domain.models.PriceKey;
import com.foodapp.price_reader.persistence.entity.PriceSnapshotIntervalEntity;
import org.springframework.stereotype.Component;

@Component
public class PriceIntervalMapper {

    private final ObjectMapper objectMapper;

    public PriceIntervalMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PriceKey toDomainKey(PriceSnapshotIntervalEntity entity) {
        return new PriceKey(
                entity.getTenantId(),
                entity.getStoreId(),
                entity.getSkuId(),
                entity.getUserSegId(),
                entity.getChannelId()
        );
    }

    public PriceInterval toDomain(PriceSnapshotIntervalEntity entity){
        return new PriceInterval(
                entity.getIntervalId(),
                toDomainKey(entity),
                entity.getStartAtUtc(),
                entity.getEndAtUtc(),
                entity.getEffectivePriceCents(),
                entity.getCurrency(),
                readJson(entity.getPriceComponentJson()),
                readJson(entity.getProvenanceJson()),
                entity.getCalcHash()
        );
    }

    public PriceSnapshotIntervalEntity toEntity(PriceInterval domain){
        return new PriceSnapshotIntervalEntity(
                domain.intervalId(),
                domain.key().tenantId(),
                domain.key().storeId(),
                domain.key().skuId(),
                domain.key().userSegId(),
                domain.key().channelId(),
                domain.startAtUtc(),
                domain.endAtUtc(),
                domain.effectivePriceCent(),
                domain.currency(),
                domain.priceComponent().toString(),
                domain.provenance().toString(),
                domain.calcHash()
        );
    }
    private JsonNode readJson(String json) {
        try{
            // ternary operator condition? true_output : false_output;
            // objectMapper.createObjectNode() => create an empty Json
            // objectMapper.readTree(json) => parse a given JSON string into a JsonNode structure
            return json == null || json.isBlank()? objectMapper.createObjectNode() : objectMapper.readTree(json);
        } catch (Exception ex){
            // catch the thrownException when the given string isn't Json, and return an empty Json
            // ensure this method only output json format
            return objectMapper.createObjectNode();
        }
    }
}
