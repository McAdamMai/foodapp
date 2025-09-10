package com.foodapp.price_reader.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.contracts.price_reader.v1.PriceResponse;
import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.domain.models.PriceKey;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import org.springframework.stereotype.Component;

@Component
public class PriceGrpcMapper {

    public PriceResponse toProto(PriceInterval domain) {
        // removed update time
        int regularPrice = getRegularPriceFromComponent(domain.priceComponent());
        return PriceResponse.newBuilder()
                .setSkuId(domain.key().skuId())
                .setCurrency(domain.currency())
                .setEffectivePriceCent(regularPrice)
                .setRegularPriceCent(domain.effectivePriceCent())
                .build();
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
}
