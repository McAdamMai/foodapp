package com.foodapp.promotion_service.mapper;

import com.foodapp.promotion_service.domain.model.Activity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.UUID;

public interface ActivityMapper {
    @Select("SELECT * FROM activity WHERE id = #{id}")
    Activity findById(UUID id);

    @Insert("""
        INSERT INTO activity (
            id, name, description, status, start_date, end_date,
            status, startDate, endDate, startDateTime,
            version, createdBy, approvedBy, rejectedBy, publishedBy, templateId 
            ) VALUES (
                #{id}, #{name}, #{description}, #{status}, #{startDate}, #{endDate},
                  #{status}, #{startDate}, #{endDate}, #{status}, #{startDate},
                  #{verison}, #{createdBy}, #{approveBy}, #{rejectBy},#{publishBy},#{templateId}
        )
    """)
    void insert(Activity activity);
}
