package com.foodapp.price_reader.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.contracts.price_reader.v1.MerchandisePriceResponse;
import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.domain.models.PriceKey;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import org.springframework.stereotype.Component;

@Component
public class PriceGrpcMapper {

    public MerchandisePriceResponse toProto(PriceInterval domain) {
        // removed update time
        int grossPrice = getGrossPriceFromComponent(domain.priceComponent());
        return MerchandisePriceResponse.newBuilder()
                .setMerchandiseUuid(domain.key().skuId())
                .setCurrency(domain.currency())
                .setGrossPrice(grossPrice)
                .setNetPrice(domain.effectivePriceCent())
                .build();
    }

    private int getGrossPriceFromComponent(JsonNode priceComponent) {
        if(priceComponent != null && priceComponent.has("grossPrice")){
            JsonNode grossPriceNode = priceComponent.get("grossPrice");
            if(grossPriceNode.isInt()){ //Ensure it's an integer
                return grossPriceNode.asInt();
            } else {
                throw new IllegalArgumentException("grossPrice must be an integer in priceComponent");
            }
        }
        throw new IllegalArgumentException("grossPrice key is missing in priceComponent");
    }
}
