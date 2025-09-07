package com.foodapp.price_reader.adapters.api.controller;

import com.foodapp.price_reader.domain.service.PriceQueryService;
import com.foodapp.price_reader.persistence.entity.MerchandisePrice;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
public class PriceReaderRestController {

    private final PriceQueryService appService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MerchandisePrice createOrUpdate(@RequestBody @Valid PriceUpsertRequest body) {
        MerchandisePrice mp = MerchandisePrice.builder()
                .merchandiseUuid(body.merchandiseUuid())
                .currency(body.currency())
                .grossPrice(body.grossPrice())
                .netPrice(body.netPrice())
                .discountStackJson("[]")
                .lastUpdate(Instant.now())
                .validFrom(Instant.now())
                .validTo(null)
                .build();

        return appService.savePrice(mp);
    }


    record PriceUpsertRequest(
            String merchandiseUuid,
            String currency,
            double grossPrice,
            double netPrice
    ) {}


    @GetMapping
    public List<MerchandisePrice> all() {
        return appService.findAll();
    }


    @GetMapping("/{id}")
    public ResponseEntity<MerchandisePrice> getById(@PathVariable("id") Long id) {
        return appService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}