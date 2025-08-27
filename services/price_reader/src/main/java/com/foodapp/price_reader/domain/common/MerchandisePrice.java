package com.foodapp.price_reader.domain.common;

import com.foodapp.price_reader.domain.models.DiscountStack;
import org.springframework.data.annotation.Id;
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
    private Instant validFrom; // store time in nanosecond since Unix epoch 2025-08-17T14:33:45.123Z
    private Instant validTo;
    private Instant lastUpdate;
}
