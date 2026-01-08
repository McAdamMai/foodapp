package com.foodapp.promotion_expander.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StackingRule {
    private int priority;
    private String behavior; // e.g., "EXCLUSIVE"
}
