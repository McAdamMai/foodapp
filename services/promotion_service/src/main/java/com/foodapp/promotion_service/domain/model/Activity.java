package com.foodapp.promotion_service.domain.model;

import com.foodapp.promotion_service.domain.model.enums.ActivityStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Activity {
    private UUID id;
    private String name;
    private String description;

    private ActivityStatus status; // enums
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    private int version;
    private String createdBy;
    private String approvedBy;
    private String rejectedBy;
    private String publishedBy;
    private UUID templateId;

    // Getter and setters
    // is Lombok's @Data is allowed
}
