package com.foodapp.pricing.application.service;

import com.foodapp.contracts.pricing.MerchandisePriceRequest;
import com.foodapp.contracts.pricing.MerchandisePriceResponse;
import com.foodapp.pricing.adapters.persistence.repository.MerchandisePriceRepository;
import com.foodapp.pricing.application.mapper.PriceMapper;
import com.foodapp.pricing.domain.models.MerchandisePrice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import java.util.Optional;

import javax.swing.text.html.Option;

@Service
@RequiredArgsConstructor
public class PricingApplicationService {

    private final MerchandisePriceRepository repo;
    private final PriceMapper mapper;

    public Optional<MerchandisePriceResponse> findPrice(String merchandiseUuId) {
        // create dummy response
        // newBuilder is for grpc
        // from domain to grpc response
        MerchandisePriceResponse response = MerchandisePriceResponse.newBuilder()
                .setMerchandiseUuid(merchandiseUuId)
                .setCurrency("CAD")
                .setAmount(19.99)
                .setDiscount(0.0)
                .setLastUpdate(currentTimestamp())
                .build();
        return Optional.of(response);
    }

    private Timestamp currentTimestamp() {
        Instant now = Instant.now(); // Get current time in UTC
        return  Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond()) // Set seconds since Jan 1, 1970
                .setNanos(now.getNano()) // Set nanoseconds, more precise time to nanosecond(10^-9)
                .build();
    }

    public Optional<MerchandisePriceResponse> findPriceDebug(String merchUuId) {
        return repo.findById(merchUuId).map(mapper::toProto); // is for map() func
    }

    public void savePrice(MerchandisePrice mp) {
        repo.save(mp);
    }
}
