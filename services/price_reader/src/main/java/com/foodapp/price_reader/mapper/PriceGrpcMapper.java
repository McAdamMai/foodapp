package com.foodapp.price_reader.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.contracts.price_reader.v1.PriceResponse;
import com.foodapp.contracts.price_reader.v1.TimelineResponse;
import com.foodapp.price_reader.domain.models.PriceInterval;
import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PriceGrpcMapper {

    public PriceGrpcMapper() {
    }

    public PriceResponse toProto(PriceInterval domain) {
        // removed update time
        int regularPrice = getRegularPriceFromComponent(domain.priceComponent());
        return PriceResponse.newBuilder()
                .setSkuId(domain.key().skuId())
                .setCurrency(domain.currency())
                .setEffectivePriceCent(domain.effectivePriceCent())
                .setRegularPriceCent(regularPrice)
                .build();
    }

    public com.foodapp.contracts.price_reader.v1.PriceInterval
    toPriceIntervalProto(com.foodapp.price_reader.domain.models.PriceInterval domain) {
        // Create the builder
        com.foodapp.contracts.price_reader.v1.PriceInterval.Builder builder =
                com.foodapp.contracts.price_reader.v1.PriceInterval.newBuilder()
                        .setIntervalId(domain.intervalId())
                        .setStartAtUtc(convertInstantToTimestamp(domain.startAtUtc()))
                        .setPriceResponse(toProto(domain));

        // Only set endAtUtc if it's not null
        if (domain.endAtUtc() != null) {
            builder.setEndAtUtc(convertInstantToTimestamp(domain.endAtUtc()));
        }

        return builder.build();
    }

    public TimelineResponse toTimelineResponseProto(
            List<com.foodapp.price_reader.domain.models.PriceInterval> domains,
            int actualLimit
    ) {
        List<com.foodapp.contracts.price_reader.v1.PriceInterval> intervals = domains.stream()
                .map(this::toPriceIntervalProto)
                .collect(Collectors.toList());

        return TimelineResponse.newBuilder()
                .addAllInterval(intervals)
                .setActualLimit(actualLimit)
                .build();
    }

    public com.foodapp.price_reader.domain.models.PriceKey
    toDomain(com.foodapp.contracts.price_reader.v1.PriceKey proto){
        return new com.foodapp.price_reader.domain.models.PriceKey(
                proto.getTenantId(),
                proto.getStoreId(),
                proto.getSkuId(),
                proto.getUserSegId(),
                proto.getChannelId()
        );
    }

    private int getRegularPriceFromComponent(JsonNode priceComponent) {
        if(priceComponent != null && priceComponent.has("regularPrice")){
            JsonNode regularPriceNode = priceComponent.get("regularPrice");
            if(regularPriceNode.isInt()){ //Ensure it's an integer
                return regularPriceNode.asInt();
            } else {
                throw new IllegalArgumentException("regularPrice must be an integer in priceComponent");
            }
        }
        throw new IllegalArgumentException("regularPrice key is missing in priceComponent");
    }

    private Timestamp convertInstantToTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
