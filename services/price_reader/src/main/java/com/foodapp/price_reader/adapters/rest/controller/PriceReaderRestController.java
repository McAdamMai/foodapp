package com.foodapp.price_reader.adapters.rest.controller;

import com.foodapp.price_reader.application.service.PriceReaderApplicationService;
import com.foodapp.price_reader.persistence.entity.MerchandisePrice;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collections;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
public class PriceReaderRestController {

    private final PriceReaderApplicationService appService;

    @PostMapping //a shortcut annotation for @RequestMapping(method = Request.POST)
    @ResponseStatus(HttpStatus.CREATED) // create an HTTP status code 201 when the method successfully completes.
    // trigger a validation on the request body based on annotations in PriceUpsertRequest
    // return a 400 bad request if validation fails
    public void createOrUpdate(@RequestBody @Valid PriceUpsertRequest body){
        // builder is for dto or model, get elements from request
        // converting request body to repo model dto
        MerchandisePrice mp = MerchandisePrice.builder()
                .merchandiseUuid(body.merchandiseUuid())
                .currency(body.currency())
                .grossPrice(body.grossPrice())
                .netPrice(body.netPrice())
                .discountStack(Collections.emptyList())
                .lastUpdate(Instant.now())
                .build();

        appService.savePrice(mp);
    }

    // Validation target and DTO, used with @Valid for automatic input validation
    record PriceUpsertRequest(
            String merchandiseUuid,
            String currency,
            double grossPrice,
            double netPrice
    ) {}

}
