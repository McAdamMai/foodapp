package com.foodapp.promotion_expander.domain.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlice {
    private UUID promotionId;
    private int version;
    private LocalDate date;
    private Instant start;
    private Instant end;
    private PromotionRules promotionRules;
}
