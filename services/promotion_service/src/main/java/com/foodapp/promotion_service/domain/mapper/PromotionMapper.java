package com.foodapp.promotion_service.domain.mapper;

import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.persistence.entity.PromotionEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

public class PromotionMapper {
    public static PromotionEntity toEntity(PromotionDomain domain) {
        if (domain == null) {return null;}
        return PromotionEntity.builder()
                .id(domain.getId().toString())
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
                .id(UUID.fromString(entity.getId()))
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

}
