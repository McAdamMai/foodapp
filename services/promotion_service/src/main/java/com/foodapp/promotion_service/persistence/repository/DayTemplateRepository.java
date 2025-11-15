package com.foodapp.promotion_service.persistence.repository;

import com.foodapp.promotion_service.persistence.entity.DayTemplateEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface DayTemplateRepository {
    DayTemplateEntity findById(String id);

    List<DayTemplateEntity> findAll();

    void create(DayTemplateEntity dayTemplateEntity);

    // modify method is pending

    void delete(String id);
}
