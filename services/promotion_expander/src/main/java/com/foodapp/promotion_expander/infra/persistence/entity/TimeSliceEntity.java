package com.foodapp.promotion_expander.infra.persistence.entity;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSliceEntity {
    private UUID id;
    private UUID promotionId;
    private Integer version;
    private LocalDate sliceDate;
    private Instant startTime;
    private Instant endTime;
    private String timezone;

    private String effectType;
    private Double effectValue;
}
