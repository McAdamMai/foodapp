package com.foodapp.promotion_service.persistence.repository;

import com.foodapp.promotion_service.persistence.entity.DayTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface DayTemplateRepository {

    void create(DayTemplateEntity dayTemplateEntity);

    DayTemplateEntity findById(@Param("id") String id);

    List<DayTemplateEntity> findAll();

    //List<DayTemplateEntity> findByName(@Param("name") String name);

    //List<DayTemplateEntity> findByCreatedBy(@Param("createdBy") String createdBy);

    List<DayTemplateEntity> search(
            @Param("name") String name,
            @Param("createdBy") String createdBy,
            @Param("description") String description,
            @Param("excludeDeleted") Boolean excludeDeleted
    );

    int update (DayTemplateEntity entity);

    int partialUpdate (DayTemplateEntity entity);

    int delete(String id);

    boolean existsById(String id);
}
