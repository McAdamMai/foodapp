package com.foodapp.price_reader.adapters.api.dto;

//record is similar to NoArgsConstructor
public record PriceKeyDto(
    String tenantId,
    String storeId,
    String skuId,
    String userSegId,
    String channelId
){}
