package com.foodapp.promotion_expander.domain.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // <--- CRITICAL: Ignores fields you don't need
// break a single nested class into several subclasses, for later calls
public class PromotionRules {

    // 1. NEEDED for Fast Update (SQL Update)
    @JsonProperty("effect")
    private EffectRule effect;

    // 2. NEEDED for Fast Update (SQL Update)
    @JsonProperty("stacking_rules") // Matches JSON snake_case
    private StackingRule stacking;

    // 3. NEEDED for Rebuild (Engine Calculation)
    @JsonProperty("schedule_rules") // Matches JSON snake_case
    private ScheduleRule schedule;

    // MISSING: "condition_rules" (e.g., Min spend $20).
    // We intentionally OMIT this because the Expander doesn't calculate cart totals.
}