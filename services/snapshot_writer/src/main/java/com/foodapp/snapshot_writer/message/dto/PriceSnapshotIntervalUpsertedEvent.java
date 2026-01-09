package com.foodapp.snapshot_writer.message.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PriceSnapshotIntervalUpsertedEvent(
        long tenantId,
        long storeId,
        long skuId,
        long userSegId,
        long channelId,
        OffsetDateTime startAtUtc,
        OffsetDateTime endAtUtc,
        BigDecimal discountValue,
        String sourceAggregateId,
        long sourceAggregateVersion
) {}
