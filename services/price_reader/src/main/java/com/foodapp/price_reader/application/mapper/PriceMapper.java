package com.foodapp.price_reader.application.mapper;

import com.foodapp.contracts.price_reader.MerchandisePriceResponse;
import com.foodapp.price_reader.domain.common.MerchandisePrice;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class PriceMapper {

    public MerchandisePriceResponse toProto(MerchandisePrice mp) {
        // toEpochMilli() gives 1755422025123 milliseconds since epoch
        // fromMills Converts to Protobuf Timestamp with seconds and nanos
        Timestamp ts = Timestamps.fromMillis(mp.getLastUpdate().toEpochMilli());
        return MerchandisePriceResponse.newBuilder() // newBuilder for proto
                .setMerchandiseUuid(mp.getMerchandiseUuid())
                .setCurrency(mp.getCurrency())
                .setGrossPrice(mp.getGrossPrice())
                .setNetPrice(mp.getNetPrice())
                .setLastUpdate(ts)
                .build();
    }

    public MerchandisePrice toDomain(MerchandisePriceResponse proto) {

        return MerchandisePrice.builder()
                .merchandiseUuid(proto.getMerchandiseUuid())
                .currency(proto.getCurrency())
                .grossPrice(proto.getGrossPrice())
                .netPrice(proto.getNetPrice())
                .discountStack(Collections.emptyList())
                // change the rpc proto to a timestamp in mills
                .lastUpdate(java.time.Instant.ofEpochMilli(
                        proto.getLastUpdate().getSeconds()*1000 +
                        proto.getLastUpdate().getNanos() / 1_000_000_000))
                .build();
    }
}
