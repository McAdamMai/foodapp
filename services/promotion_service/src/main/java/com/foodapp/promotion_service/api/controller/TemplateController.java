package com.foodapp.promotion_service.api.controller;

import com.foodapp.promotion_service.api.controller.dto.request.TemplateDtoRequest;
import com.foodapp.promotion_service.api.controller.dto.request.TemplateUpdateRequest;
import com.foodapp.promotion_service.api.controller.dto.response.TemplateDtoResponse;
import com.foodapp.promotion_service.domain.model.DayTemplateDomain;
import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.domain.service.TemplateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/template")
@RequiredArgsConstructor
@Slf4j
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateDtoResponse createTemplate(
            @RequestBody @Valid TemplateDtoRequest request
            ) {

        log.info("Create Template: {}", request);

        DayTemplateDomain domain = templateService.create(
                request.name(),
                request.description(),
                request.ruleJson(),
                request.createdBy()
        );

        return TemplateDtoResponse.from(domain);
    }

    @GetMapping
    public List<TemplateDtoResponse> getAllTemplates() {
        log.info("Get All Templates");

        return templateService.findAll()
                .stream()
                .map(TemplateDtoResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public TemplateDtoResponse getTemplateById(@PathVariable UUID id) {
        log.info("Get Template: {}", id);
        return TemplateDtoResponse.from(templateService.findById(id));
    }

    @PatchMapping
    public TemplateDtoResponse updateTemplate(@RequestBody @Valid TemplateUpdateRequest request) {
        log.info("Update Template: {}", request);

        return TemplateDtoResponse.from(templateService.update(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @NotNull UUID id
    ) {
        log.info("Deleting day template: {}", id);

        templateService.delete(id);
    }

}
