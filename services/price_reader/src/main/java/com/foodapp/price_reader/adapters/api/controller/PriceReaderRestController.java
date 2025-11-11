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
   @DeleteMapping("/delete")
   public ResponseEntity<Void> deleteBySku(@RequestParam String skuId){
        restService.deleteBySkuId(skuId);
        return ResponseEntity.noContent().build();
   }

   @PutMapping("/{id}")
   public ResponseEntity<String> updatePrice(@PathVariable String id,@RequestBody PriceIntervalDto dto){
        restService.updateInterval(id, dto);
        return ResponseEntity.ok("Price interval updated successfully");
   }

   @GetMapping("/{skuId}/history")
   public ResponseEntity<List<PriceIntervalDto>> getPriceHistory(@PathVariable String skuId){
        List<PriceIntervalDto> history = restService.getPriceHistory(skuId).stream()
                .map(dtoMapper::toDto)
                .toList();

        if (history.isEmpty()) {
            return  ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(history);
   }

   @GetMapping("/range")
    public  ResponseEntity<List<PriceIntervalDto>> getPricesInRange(
            @RequestParam int min,
            @RequestParam int max
   ){
        List<PriceIntervalDto> prices = restService.getPricesInRange(min, max).stream()
                .map(dtoMapper::toDto)
                .toList();

        if (prices.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(prices);

   }

    @GetMapping("/lookup")
    public ResponseEntity<?> lookupPrice(
            @RequestParam String skuId,
            @RequestParam(required = false) String at
    ) {
        // ✅ Condition 1: Input with timestamp, search price in the indicated time range
        if (at != null && !at.isBlank()) {
            Instant atInstant;
            try {
                atInstant = Instant.parse(at);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Invalid 'at' timestamp format");
            }

            return restService.findPrice(skuId, atInstant)
                    .map(dtoMapper::toDto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NO_CONTENT).build());

        }

        // ✅ conditon2: input has no timestamp, return all price history
        List<PriceIntervalDto> history = restService.getPriceHistory(skuId).stream()
                .map(dtoMapper::toDto)
                .toList();

        if (history.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No price history found");
        }

        return ResponseEntity.ok(history);
    }



}