package com.foodapp.promotion_service.domain.mapper;

import com.foodapp.promotion_service.domain.model.DayTemplateDomain;
import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.domain.model.PromotionOutboxDomain;
import com.foodapp.promotion_service.persistence.entity.DayTemplateEntity;
import com.foodapp.promotion_service.persistence.entity.PromotionEntity;
import com.foodapp.promotion_service.persistence.entity.PromotionOutboxEntity;

import java.util.UUID;

public class PromotionMapper {
    public static PromotionEntity toEntity(PromotionDomain domain) {
        if (domain == null) {return null;}
        return PromotionEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .status(domain.getStatus())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .createAt(domain.getCreateAt())
                .updateAt(domain.getUpdateAt())
                .version(domain.getVersion()) // Crucial for optimistic locking
                .createdBy(domain.getCreatedBy())
                .reviewedBy(domain.getReviewedBy())
                .publishedBy(domain.getPublishedBy())
                .templateId(domain.getTemplateId())
                .build();
    }

    public static PromotionDomain toDomain(PromotionEntity entity){
        if (entity == null) {return null;}
        return PromotionDomain.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .createAt(entity.getCreateAt())
                .updateAt(entity.getUpdateAt())
                .version(entity.getVersion()) // Pass the version to the domain model
                .createdBy(entity.getCreatedBy())
                .reviewedBy(entity.getReviewedBy())
                .publishedBy(entity.getPublishedBy())
                .templateId(entity.getTemplateId())
                .build();
    }

    public static DayTemplateEntity toEntity (DayTemplateDomain domain) {
        if (domain == null) {return null;}
        return DayTemplateEntity.builder()
                // 3. No more .toString() needed!
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .ruleJson(domain.getRuleJson())
                .createdBy(domain.getCreateBy())
                .createdAt(domain.getCreateAt())
                .build();
    }

    public static DayTemplateDomain toDomain (DayTemplateEntity entity) {
        if (entity == null) {return null;}
        return DayTemplateDomain.builder()
                // 3. No more UUID.fromString() needed!
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .ruleJson(entity.getRuleJson())
                .createBy(entity.getCreatedBy())
                .createAt(entity.getCreatedAt())
                .build();
    }

    public static PromotionOutboxEntity toEntity(PromotionOutboxDomain domain) {
        if (domain == null) {
            return null;
        }

        return PromotionOutboxEntity.builder()
                .id(domain.getId())
                .aggregateId(domain.getAggregateId())
                .aggregateVersion(domain.getAggregateVersion())
                .changeMask(domain.getChangeMask())
                .payload(domain.getPayload())
                .publishedAt(domain.getPublishedAt())
                .build();
    }

    public static PromotionOutboxDomain toDomain(PromotionOutboxEntity entity) {
        if (entity == null) {
            return null;
        }

        return PromotionOutboxDomain.builder()
                .id(entity.getId())
                .aggregateId(entity.getAggregateId())
                .aggregateVersion(entity.getAggregateVersion())
                .changeMask(entity.getChangeMask())
                .payload(entity.getPayload())
                .publishedAt(entity.getPublishedAt())
                .build();
    }

}
