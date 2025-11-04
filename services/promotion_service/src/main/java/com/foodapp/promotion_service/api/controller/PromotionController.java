package com.foodapp.promotion_service.api.controller;


import com.foodapp.promotion_service.api.controller.dto.request.PromotionCreationDtoRequest;
import com.foodapp.promotion_service.api.controller.dto.response.PromotionSummaryDtoResponse;
import com.foodapp.promotion_service.api.controller.dto.response.PromotionDetailDtoResponse;
import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.domain.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

        private final ActivityService activityService;

        @PostMapping("/create")
        @ResponseStatus(HttpStatus.CREATED)
        // small scalar inputs use @RequestParam
        public PromotionSummaryDtoResponse createPromotion(@RequestBody @Valid PromotionCreationDtoRequest dto) {
            return PromotionSummaryDtoResponse.from(
                    activityService.create(
                            dto.name(),
                            dto.description(),
                            dto.startDate(),
                            dto.endDate(),
                            dto.createdBy(),
                            dto.templateId()));
        }

        // Get all the promotions
        @GetMapping("/all")
        public List<PromotionSummaryDtoResponse> getAllPromotions() {
            return activityService.findAll()
                    .stream()
                    .map(PromotionSummaryDtoResponse::from)
                    .toList();
        }

        @GetMapping("/{id}")
        public PromotionDetailDtoResponse getPromotionById(@PathVariable UUID id) {
            PromotionDomain domain = activityService.findById(id);
            return PromotionDetailDtoResponse.from(domain);
        }
}
