package com.foodapp.price_reader.adapters.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodapp.price_reader.domain.models.PriceKey;

import java.time.Instant;

public record PriceIntervalDto(
        // In dto, startAtUtc and endAtUtc are converted to String to allow better understanding across diff language
        String intervalId,
        PriceKeyDto key,
        String startAtUtc, //ISO-8601 string
        String endAtUtc, // nullable
        int effectivePriceCent,
        String currency,
        JsonNode priceComponent,
        JsonNode provenance,
        String calcHash // nullable
) {


}
