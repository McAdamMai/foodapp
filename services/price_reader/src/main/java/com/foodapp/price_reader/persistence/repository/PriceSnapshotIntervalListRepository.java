package com.foodapp.price_reader.persistence.repository;

import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.persistence.entity.PriceSnapshotIntervalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PriceSnapshotIntervalListRepository {
    // The old method (to be deprecated or removed)
    // Optional<Price> getPrice(String skuId, Instant at);
    /**
     * Fetches all prices for the given list of SKU IDs that are valid at the specified time.
     *
     * @param skuIds The list of SKU IDs to find prices for.
     * @param at The timestamp to check for price validity.
     * @return A Map where the key is the skuId and the value is the corresponding Price.
     *         SKUs for which no price was found will be absent from the map.
     */
    Map<String, Optional<PriceInterval>> getSnapshotPriceList(List<String> skuIds, Instant at);
}
