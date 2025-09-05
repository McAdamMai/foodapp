package com.foodapp.price_reader.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Immutable
@Table(
        name = "price_snapshot_interval",
        indexes = {
                @Index(
                        name = "idx_tenant_store_sku_user_channel_end", //index name
                        columnList = "tenant_id, store_id, user_seg_id, channel_id, end_at_utc" // column name
                ),
                @Index(
                        name = "idx_tenant_store_sku_user_channel_start", //index name
                        columnList = "tenant_id, store_id, user_seg_id, channel_id, start_at_utc" // column name
                )
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uq_price_snapshot_key_start",
                columnNames = {
                        "tenant_id","store_id","sku_id","user_seg_id","channel_id","start_at_utc"
                }
        )
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceSnapshotIntervalEntity {
    @Id
    @Column(name = "interbal_id", nullable = false, updatable = false, length = 36)
    private String intervalId; // UUID stored as VARCHAR(36)

    @Column(name = "tenant_id", nullable = false, updatable = false, length = 64)
    private String tenantId;

    @Column(name = "store_id", nullable = false, updatable = false, length = 64)
    private String storeId;

    @Column(name = "sku_id", nullable = false, updatable = false, length = 128)
    private String skuId;

    @Column(name = "user_seg_id", updatable = false, length = 64)
    private String userSegId; // nullable

    @Column(name = "channel_id", updatable = false, length = 64)
    private String channelId; // nullable

    @Column(name = "start_at_utc", nullable = false, updatable = false)
    private Instant startAtUtc;

    @Column(name = "start_at_utc")
    private Instant endAtUtc;

    // Store cents as INT
    @Column(name = "effective_price_cents", nullable = false)
    private int effectivePriceCents;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "price_components", columnDefinition = "JSON")
    private String priceComponentJson;

    @Column(name = "provenance", columnDefinition = "JSON")
    private String provenanceJson;

    @Column(name = "calc_hash", length = 64)
    private String calcHash;

    @Override
    // entity1.equals(entity2) => Objects.equals(entity1.intervalID, entity2.intervalID)
    public boolean equals(Object o) {
        if (this == o) return true; // check if two objects are the exact the same in memory
        if (!(o instanceof PriceSnapshotIntervalEntity that)) return false; // if o is not an instance of PriceSnapshotIntervalEntity
        return Objects.equals(intervalId, that.intervalId);
    }

    @Override
    // generate hashCode based on intervalID
    public int hashCode() {
        return Objects.hash(intervalId);
    }
}
