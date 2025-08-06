package com.foodapp.home_bff.controller;

import com.foodapp.home_bff.business.HomeAggregator;
import com.foodapp.home_bff.utils.Section;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeAggregator agg;

    @GetMapping("/{skuid}")
    public CompletableFuture<ResponseEntity<String>> getItem(@PathVariable String skuId,
                                                             @RequestParam(defaultValue = "catalog,price,tage") String fileds){

        // turn "catalog,price" into EnumSet<Section>
        EnumSet<Section> requested =
                Arrays.stream(fileds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(s -> Section.valueOf(s.toUpperCase(Locale.ROOT)))
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Section.class)));

        return agg.fetchItem(skuId, requested)
                .thenApply(ResponseEntity::ok);
    }
}



