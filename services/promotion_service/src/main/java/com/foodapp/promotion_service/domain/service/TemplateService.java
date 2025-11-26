package com.foodapp.promotion_service.domain.service;

import com.foodapp.promotion_service.api.controller.dto.request.PromotionUpdateRequest;
import com.foodapp.promotion_service.api.controller.dto.request.TemplateUpdateRequest;
import com.foodapp.promotion_service.domain.mapper.PromotionMapper;
import com.foodapp.promotion_service.domain.model.DayTemplateDomain;
import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.persistence.entity.DayTemplateEntity;
import com.foodapp.promotion_service.persistence.repository.DayTemplateRepository;
import com.foodapp.exception.TemplateNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TemplateService {
    private final DayTemplateRepository repo;

    // ========== Create ==========
    @Transactional
    public DayTemplateDomain create(
            String name,
            String description,
            String ruleJson,
            String createdBy
    ) {
        DayTemplateDomain newTemplate = DayTemplateDomain.createNewTemplate(name, description, ruleJson, createdBy);
        repo.create(PromotionMapper.toEntity(newTemplate));
        return newTemplate;
    }

    @Transactional
    public DayTemplateDomain update(TemplateUpdateRequest request) {
        if (!request.hasUpdate()) {
            throw new IllegalArgumentException("No fields to update");
        }
        DayTemplateEntity newEntity = buildEntity(request);

        int rowAffected = repo.partialUpdate(newEntity);

        if (rowAffected == 0) {
            throw new IllegalArgumentException("No rows affected");
        }

        return loadDomain(request.getId());
    }

    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id.toString())) {
            throw new TemplateNotFoundException("Template not found: " + id);
        }

        int deleted = repo.delete(id.toString());

        if (deleted == 0) {
            throw new TemplateNotFoundException("Template not found: " + id);
        }
    }

    // ========== QUERIES ==========
    /**
     * Gets available actions for a promotion based on user role.
     */
    public List<DayTemplateDomain> findAll() {

        return repo.findAll()
                .stream()
                .map(PromotionMapper::toDomain)
                .toList();
    }

    public DayTemplateDomain findById(UUID id) {
        return loadDomain(id);
    }

    // ========== Private helper service ==========
    private DayTemplateDomain loadDomain(UUID id) {
        return PromotionMapper.toDomain(
                repo.findById(id.toString())
        );
    }

    private DayTemplateEntity buildEntity(TemplateUpdateRequest request) {
        DayTemplateEntity.DayTemplateEntityBuilder builder = DayTemplateEntity.builder()
                .id(request.getId().toString());

        if (request.getName() != null) {
            builder.name(request.getName());
        }

        if (request.getDescription() != null) {
            builder.description(request.getDescription());
        }

        if (request.getRuleJson() != null) {
            builder.ruleJson(request.getRuleJson());
        }

        if (request.getCreatedBy() != null) {
            builder.createdBy(request.getCreatedBy());
        }

        return builder.build();
    }
}
