package com.foodapp.promotion_service.domain.service;

import com.foodapp.promotion_service.domain.mapper.PromotionMapper;
import com.foodapp.promotion_service.domain.model.DayTemplateDomain;
import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.persistence.repository.DayTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TemplateService {
    private final DayTemplateRepository repo;

    @Transactional
    public DayTemplateDomain create(
            String name,
            String description,
            String ruleJson,
            String createdBy
    ) {
        DayTemplateDomain newTemplate = DayTemplateDomain.createNewTemplate(name, description, ruleJson, createdBy);
        repo.create(PromotionMapper.toEnity(newTemplate));
        return newTemplate;
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

    private DayTemplateDomain loadDomain(UUID id) {
        return PromotionMapper.toDomain(
                repo.findById(id.toString())
        );
    }
}
