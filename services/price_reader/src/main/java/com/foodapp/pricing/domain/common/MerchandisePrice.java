package com.foodapp.pricing.domain.models;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

@Document(collection = "merchandise_price_snapshot")
@Data
@Builder
public class MerchandisePrice {
    @Id
    private String merchandiseUuid;
    private double grossPrice;
    private double netPrice;
    private String currency;
    private List<DiscountStack> discountStack;
    private Instant lastUpdate; // store time in nanosecond since Unix epoch 2025-08-17T14:33:45.123Z
}
