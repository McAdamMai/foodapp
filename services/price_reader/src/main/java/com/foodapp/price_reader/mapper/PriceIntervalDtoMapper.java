package com.foodapp.price_reader.mapper;

import com.foodapp.price_reader.adapters.api.dto.PriceIntervalDto;
import com.foodapp.price_reader.adapters.api.dto.PriceKeyDto;
import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.domain.models.PriceKey;
import org.springframework.stereotype.Component;

@Component
public class PriceIntervalDtoMapper {

    public PriceKeyDto toDto(PriceKey domain) {
        return new PriceKeyDto(
                domain.tenantId(),
                domain.storeId(),
                domain.skuId(),
                domain.userSegId(),
                domain.channelId()
        );
    }

    public PriceIntervalDto toDto(PriceInterval domain) {
        return new PriceIntervalDto(
                domain.intervalId(),
                toDto(domain.key()),
                domain.startAtUtc().toString(),
                domain.endAtUtc() != null? domain.endAtUtc().toString() : null,
                domain.effectivePriceCent(),
                domain.currency(),
                domain.priceComponent(),
                domain.provenance(),
                domain.calcHash()
        );
    }
}
