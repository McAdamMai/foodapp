package com.foodapp.price_reader.adapters.api.controller;


import com.foodapp.price_reader.adapters.api.dto.PriceIntervalDto;
import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.domain.models.PriceKey;
import com.foodapp.price_reader.domain.service.AdminRestfulService;
import com.foodapp.price_reader.mapper.PriceIntervalDtoMapper;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;


import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
public class PriceReaderRestController {

    private final AdminRestfulService restService;
    private final PriceIntervalDtoMapper dtoMapper;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<PriceIntervalDto> createOrUpdate(@RequestBody @Valid List<PriceIntervalDto> dtos) {
        List<PriceInterval> domains = dtos.stream()
                .map(dtoMapper::toDomain)
                .toList();
        List<PriceInterval> saved = restService.saveBatchPrices(domains);

        return  saved.stream()
                .map(dtoMapper::toDto)
                .toList();

    }
    @GetMapping("/findPrice")
    public ResponseEntity<PriceIntervalDto> findPrice(
            @RequestParam String skuId,
            @RequestParam String currency,
            @RequestParam String at
    ){
        Instant atInstant = Instant.parse(at);
        return restService.findPrice(skuId,atInstant)
                .map(dtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());


    }

   @GetMapping("/timeline")
    public ResponseEntity<List<PriceIntervalDto>> getTimeline(
            @RequestParam("tenantId") String tenantId,
            @RequestParam("storeId") String storeId,
            @RequestParam("skuId") String skuId,
            @RequestParam(value = "userSegId", required = false) String userSegId,
            @RequestParam(value = "channelId", required = false) String channelId,
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam(value = "limit", defaultValue = "100") int limit

   ){
       PriceKey key= new PriceKey(tenantId, storeId, skuId, userSegId, channelId);

       Instant fromInstant = Instant.parse(from);
       Instant toInstant = Instant.parse(to);
       if(!fromInstant.isBefore(toInstant)){
           throw new IllegalArgumentException("from must be before to");
       }
       List<PriceIntervalDto> result = restService.getTimeline(key, fromInstant, toInstant, limit)
               .stream()
               .map(dtoMapper::toDto)
               .toList();

       return ResponseEntity.ok(result);
   }


}