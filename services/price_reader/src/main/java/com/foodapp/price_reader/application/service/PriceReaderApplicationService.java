package com.foodapp.price_reader.application.service;

import com.foodapp.contracts.price_reader.MerchandisePriceResponse;
import com.foodapp.price_reader.adapters.persistence.repository.MerchandisePriceRepository;
import com.foodapp.price_reader.application.mapper.PriceMapper;
import com.foodapp.price_reader.domain.common.MerchandisePrice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PriceReaderApplicationService {

    private final MerchandisePriceRepository repo;
    private final PriceMapper mapper;

    // === gRPC 方法（暂时还是 dummy 响应） ===
    public Optional<MerchandisePriceResponse> findPrice(String merchandiseUuId, String currency, Timestamp at) {
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
        Instant now = Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();
    }

    // === gRPC Debug 方法，调用 JPA 查询 ===
    public Optional<MerchandisePriceResponse> findPriceDebug(String merchandiseUuId, String currency, Timestamp at) {
        Instant atInstant = Instant.ofEpochSecond(at.getSeconds(), at.getNanos());
        return repo.findByMerchandiseUuidAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                merchandiseUuId, atInstant, atInstant
        ).map(mapper::toProto);
    }

    // === RESTful 方法 ===
    public MerchandisePrice savePrice(MerchandisePrice mp) {
        return repo.save(mp);
    }

    public Optional<MerchandisePrice> findById(Long id) {
        return repo.findById(id);
    }

    public List<MerchandisePrice> findAll() {
        return repo.findAll();
    }
}

