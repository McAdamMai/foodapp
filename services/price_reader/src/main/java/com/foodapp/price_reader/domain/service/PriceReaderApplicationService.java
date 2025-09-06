package com.foodapp.price_reader.domain.service;

import com.foodapp.contracts.price_reader.v1.MerchandisePriceResponse;
import com.foodapp.price_reader.mapper.PriceIntervalMapper;
import com.foodapp.price_reader.mapper.PriceGrpcMapper;
import com.foodapp.price_reader.persistence.entity.MerchandisePrice;
import com.foodapp.price_reader.persistence.repository.MerchandisePriceRepository;
import com.foodapp.price_reader.persistence.repository.PriceSnapshotIntervalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PriceReaderApplicationService {

    private final PriceSnapshotIntervalRepository repo;
    private final MerchandisePriceRepository merchandiseRepo;
    private final PriceIntervalMapper domainMapper;
    private final PriceGrpcMapper grpcMapper;
    //https://poe.com/s/dKxtsOkluHPXl6jONJI7 following this to implement lookup

    //dummy response method for gRPC
    public Optional<MerchandisePriceResponse> findPriceDebug(String merchandiseUuId, String currency, Timestamp at) {
        // create dummy response
        // newBuilder is for grpc
        // from domain to grpc response
        MerchandisePriceResponse response = MerchandisePriceResponse.newBuilder()
                .setMerchandiseUuid(merchandiseUuId)
                .setCurrency("CAD")
                .setGrossPrice(19.99)
                .setNetPrice(16.99)
                .build();
        return Optional.of(response);
    }

    public Optional<MerchandisePriceResponse> findPrice(String merchandiseUuId, String currency, Timestamp at) {
        Instant atInstant = Instant.ofEpochSecond(at.getSeconds(), at.getNanos());
        return repo.findByValidPriceForInstant(merchandiseUuId, atInstant)
                .map(domainMapper::toDomain)
                .map(grpcMapper::toProto);// is for map() func
    }

    // Method for restful
    public MerchandisePrice savePrice(MerchandisePrice mp) {
        return merchandiseRepo.save(mp);
    }

    public Optional<MerchandisePrice> findById(Long id) {
        return merchandiseRepo.findById(id);
    }

    public List<MerchandisePrice> findAll() {
        return merchandiseRepo.findAll();
    }

}
