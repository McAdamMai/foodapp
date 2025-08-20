package com.foodapp.pricing.domain.models;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "merchandise_price")
@Data
@Builder
public class MerchandisePrice {
    @Id
    private String merchandiseUuid;

    private String currency;
    private double amount;
    private double discount;
    private Instant lastUpdate; // store time in nanosecond since Unix epoch 2025-08-17T14:33:45.123Z
}
