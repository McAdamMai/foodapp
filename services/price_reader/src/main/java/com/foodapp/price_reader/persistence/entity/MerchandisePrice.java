package com.foodapp.price_reader.persistence.entity;
import com.foodapp.price_reader.domain.models.DiscountStack;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name="merchandise_price_snapshot")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchandisePrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private long id; // this is now a unique, auto-generated ID for this snapshot

    @Column(name = "merchandise_uuid", nullable = false, length = 64)
    private String merchandiseUuid;

    @Column(name = "gross_price", nullable = false)
    private double grossPrice;

    @Column(name = "net_price", nullable = false)
    private double netPrice;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to", nullable = true)
    private Instant validTo;

    @Column(name = "last_update", nullable = false)
    private Instant lastUpdate;

    @Column(name = "discount_stack", columnDefinition = "JSON")
    private String discountStackJson;
}