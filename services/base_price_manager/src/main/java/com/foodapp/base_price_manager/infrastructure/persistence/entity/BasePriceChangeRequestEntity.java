package com.foodapp.base_price_manager.infrastructure.persistence.entity;

import com.foodapp.base_price_manager.domain.fsm.BasePriceStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


@Entity
@Table(name = "base_price_change_request")
public class BasePriceChangeRequestEntity {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private BasePriceStatus status;

    @Column (nullable = false, updatable = false)
    private String createdBy;

    @Column (nullable = false, updatable = false)
    private java.time.Instant createdAt;

    @Column (nullable = false)
    private String updatedBy;

    @Column (nullable = false)
    private java.time.Instant updatedAt;

    @Column (nullable = false)
    private  Long tenantId;

    @Column (nullable = false)
    private Long storeId;

    @Column (nullable = false)
    private Long skuId;

    @Column (nullable = false)
    private Long userSegId;

    @Column
    private Long channelId;

    @Column(nullable = false)
    private java.time.Instant startAtUtc;

    @Column(nullable = false)
    private java.math.BigDecimal basePrice;

    @Column
    private String reason;

    @Column
    private String rejectReason;

    protected BasePriceChangeRequestEntity(){}

    public static BasePriceChangeRequestEntity createSubmitted(
            Long tenantId,
            Long storeId,
            Long skuId,
            Long userSegId,
            Long channelId,
            java.time.Instant startAtUtc,
            java.math.BigDecimal basePrice,
            String reason,
            String operator,
            java.time.Instant now
    ) {
        BasePriceChangeRequestEntity e = new BasePriceChangeRequestEntity();
        e.status = BasePriceStatus.SUBMITTED;

        e.tenantId = tenantId;
        e.storeId = storeId;
        e.skuId = skuId;
        e.userSegId = userSegId;
        e.channelId = channelId;

        e.startAtUtc = startAtUtc;
        e.basePrice = basePrice;

        e.reason = reason;
        e.rejectReason = null;

        e.createdBy = operator;
        e.createdAt = now;
        e.updatedBy = operator;
        e.updatedAt = now;

        return e;
    }


}
