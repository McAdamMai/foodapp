package com.foodapp.pricing.application.mapper;

import com.foodapp.contracts.pricing.MerchandisePriceResponse;
import com.foodapp.pricing.domain.models.MerchandisePrice;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import org.springframework.stereotype.Component;

@Component
public class PriceMapper {

    public MerchandisePriceResponse toProto(MerchandisePrice mp) {
        // toEpochMilli() gives 1755422025123 milliseconds since epoch
        // fromMills Converts to Protobuf Timestamp with seconds and nanos
        Timestamp ts = Timestamps.fromMillis(mp.getLastUpdate().toEpochMilli());
        return MerchandisePriceResponse.newBuilder() // newBuilder for proto
                .setMerchandiseUuid(mp.getMerchandiseUuid())
                .setCurrency(mp.getCurrency())
                .setAmount(mp.getAmount())
                .setDiscount(mp.getDiscount())
                .setLastUpdate(ts)
                .build();
    }

    public MerchandisePrice toDomain(MerchandisePriceResponse proto) {

        return MerchandisePrice.builder()
                .merchandiseUuid(proto.getMerchandiseUuid())
                .currency(proto.getCurrency())
                .amount(proto.getAmount())
                .discount(proto.getDiscount())
                // change the rpc proto to a timestamp in mills
                .lastUpdate(java.time.Instant.ofEpochMilli(
                        proto.getLastUpdate().getSeconds()*1000 +
                        proto.getLastUpdate().getNanos() / 1_000_000_000))
                .build();
    }
}
