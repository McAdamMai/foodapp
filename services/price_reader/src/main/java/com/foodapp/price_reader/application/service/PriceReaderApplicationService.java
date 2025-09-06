package com.foodapp.price_reader.application.service;

import com.foodapp.contracts.price_reader.MerchandisePriceResponse;
import com.foodapp.price_reader.adapters.persistence.repository.MerchandisePriceRepository;
import com.foodapp.price_reader.application.mapper.PriceMapper;
import com.foodapp.price_reader.persistence.entity.MerchandisePrice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PriceReaderApplicationService {

    private final MerchandisePriceRepository repo;
    private final PriceMapper mapper;
    //https://poe.com/s/dKxtsOkluHPXl6jONJI7 following this to implement lookup

    //method for GRPC
    public Optional<MerchandisePriceResponse> findPrice(String merchandiseUuId, String currency, Timestamp at) {
        // create dummy response
        // newBuilder is for grpc
        // from domain to grpc response
        MerchandisePriceResponse response = MerchandisePriceResponse.newBuilder()
                .setMerchandiseUuid(merchandiseUuId)
                .setCurrency("CAD")
                .setGrossPrice(19.99)
                .setNetPrice(16.99)
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

    public Optional<MerchandisePriceResponse> findPriceDebug(String merchandiseUuId, String currency, Timestamp at) {
        Instant atInstant = Instant.ofEpochSecond(at.getSeconds(), at.getNanos());
        return repo.findByValidPriceForInstant(merchandiseUuId, atInstant).map(mapper::toProto); // is for map() func
    }

    // Method for restful
    public void savePrice(MerchandisePrice mp) {
        repo.save(mp);
    }

}
