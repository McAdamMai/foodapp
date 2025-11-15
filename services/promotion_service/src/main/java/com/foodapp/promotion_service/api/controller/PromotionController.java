package com.foodapp.promotion_service.api.controller;

import com.foodapp.promotion_service.api.controller.dto.request.PromotionUpdateRequest;
import com.foodapp.promotion_service.fsm.PromotionEvent;
import com.foodapp.promotion_service.fsm.UserRole;
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

    @PutMapping("/update")
    public PromotionDetailDtoResponse updateDetails(
            @RequestBody @Valid PromotionUpdateRequest request
    ){
        PromotionDomain updated = activityService.updateDetails(request);
        return PromotionDetailDtoResponse.from(updated);
    }

    @PostMapping("/{id}/submit")
    public PromotionDetailDtoResponse submit(
            @PathVariable UUID id,
            @RequestParam String submittedBy
    ){
        PromotionDomain domain = activityService.submit(id, submittedBy);
        return PromotionDetailDtoResponse.from(domain);
    }

    @PostMapping("/{id}/approve")
    public PromotionDetailDtoResponse approve(
            @PathVariable UUID id,
            @RequestParam String reviewedBy
    ){
        PromotionDomain domain = activityService.approve(id, reviewedBy);
        return PromotionDetailDtoResponse.from(domain);
    }

    @PostMapping("/{id}/reject")
    public PromotionDetailDtoResponse reject(
            @PathVariable UUID id,
            @RequestParam String reviewedBy
    ){
        PromotionDomain domain = activityService.reject(id, reviewedBy);
        return PromotionDetailDtoResponse.from(domain);
    }

    @PostMapping("/{id}/publish")
    public PromotionDetailDtoResponse publish(
            @PathVariable UUID id,
            @RequestParam String publishedBy
    ){
        PromotionDomain domain = activityService.publish(id, publishedBy);
        return PromotionDetailDtoResponse.from(domain);
    }

    @PostMapping("/{id}/rollback")
    public PromotionDetailDtoResponse rollback(
            @PathVariable UUID id,
            @RequestParam String rolledBackBy,
            @RequestParam UserRole role
    ){
        PromotionDomain domain = activityService.rollBack(id, rolledBackBy, role);
        return PromotionDetailDtoResponse.from(domain);
    }

    @GetMapping("/{id}/actions")
    public List<PromotionEvent> getAvailableActions(
            @PathVariable UUID id,
            @RequestParam UserRole role
    ){
        return activityService.getAvailableActions(id, role);
    }







}
