package com.foodapp.promotion_service.persistence.repository;

import com.foodapp.promotion_service.persistence.entity.DayTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface DayTemplateRepository {

    void create(DayTemplateEntity dayTemplateEntity);

    // 2. Change parameter to UUID
    Optional<DayTemplateEntity> findById(@Param("id") UUID id);

    List<DayTemplateEntity> findAll();

    List<DayTemplateEntity> search(
            @Param("name") String name,
            @Param("createdBy") String createdBy,
            @Param("description") String description,
            @Param("excludeDeleted") Boolean excludeDeleted
    );

    int update (DayTemplateEntity entity);

    int partialUpdate (DayTemplateEntity entity);

    // 2. Change parameter to UUID
    int delete(UUID id);

    // 2. Change parameter to UUID
    boolean existsById(UUID id);
}