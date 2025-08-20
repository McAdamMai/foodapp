package com.foodapp.pricing.adapters.rest.controller;

import com.foodapp.pricing.application.service.PricingApplicationService;
import com.foodapp.pricing.domain.models.MerchandisePrice;
import io.micrometer.core.instrument.config.validate.Validated;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
public class PricingRestController {

    private final PricingApplicationService appService;

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
                .amount(body.amount())
                .discount(body.discount())
                .lastUpdate(Instant.now())
                .build();

        appService.savePrice(mp);
    }

    // Validation target and DTO, used with @Valid for automatic input validation
    record PriceUpsertRequest(
            String merchandiseUuid,
            String currency,
            double amount,
            double discount
    ) {}

}
