package com.foodapp.price_reader.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;


import java.time.Instant;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true) // enable backward compatibility
@Builder
public record PriceInterval(
        String intervalId,
        PriceKey key,
        Instant startAtUtc,
        Instant endAtUtc, // nullable
        int effectivePriceCent,
        String currency,
        JsonNode priceComponent,
        JsonNode provenance,
        String calcHash // nullable
) {
    //Predefined allowed keys
    private static final Set<String> MANDATORY_KEYS = Set.of("regularPrice", "taxRate");

    public PriceInterval {
        if (intervalId == null || startAtUtc == null){
            throw new IllegalArgumentException("IntervalId and startAtUtc are required");
        }
        if (priceComponent !=null){
            validatePriceComponent(priceComponent);
        }
    }

    private static void validatePriceComponent(JsonNode priceComponent) {
        // Check if all mandatory keys are present
        for (String mandatoryKey : MANDATORY_KEYS){
            if (!priceComponent.has(mandatoryKey)){
                throw new IllegalArgumentException("Missing mandatory key in priceComponent: " + mandatoryKey);
            }
        }
        priceComponent.fields().forEachRemaining(entry ->{
           String key = entry.getKey();

           if(MANDATORY_KEYS.contains(key)){
               if (!entry.getValue().isNumber()){
                   throw new IllegalArgumentException("Mandatory key '" + key + "' is not a number");
               }
           }
        });
    }

    public boolean isOpenEnded(){
        return endAtUtc == null;
    }
    public boolean covers(Instant t){
        // t >= start && (t < before or end is open)
        // time.isAfter(t) means time > t
        return !startAtUtc.isAfter(t) && endAtUtc.isAfter(t) || isOpenEnded();
    }

    public boolean overlaps(Instant from, Instant toExclusive){
        // verify if two periods are overlapping or not
        return (endAtUtc == null || endAtUtc.isAfter(from)) && startAtUtc.isBefore(toExclusive);
    }
}
