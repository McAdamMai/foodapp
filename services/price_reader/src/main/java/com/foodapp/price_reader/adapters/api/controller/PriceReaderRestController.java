package com.foodapp.price_reader.adapters.api.controller;


import com.foodapp.price_reader.adapters.api.dto.PriceIntervalDto;
import com.foodapp.price_reader.domain.models.PriceInterval;
import com.foodapp.price_reader.domain.service.AdminRestfulService;
import com.foodapp.price_reader.mapper.PriceIntervalDtoMapper;
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

    private final AdminRestfulService restService;
    private final PriceIntervalDtoMapper dtoMapper;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PriceIntervalDto createOrUpdate(@RequestBody @Valid PriceIntervalDto dto) {
        PriceInterval domain = dtoMapper.toDomain(dto);
        PriceInterval saved = restService.savePrice(domain);
        return dtoMapper.toDto(saved);
    }
    @GetMapping
    public void all() {
    }
    @GetMapping("/{id}")
    public void getById(@PathVariable("id") Long id) {
    }
}