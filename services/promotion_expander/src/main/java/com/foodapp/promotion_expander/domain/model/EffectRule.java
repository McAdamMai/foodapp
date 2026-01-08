package com.foodapp.promotion_expander.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EffectRule {
    private String type; // e.g., "PERCENTAGE_OFF_ORDER"
    private BigDecimal value;
}
