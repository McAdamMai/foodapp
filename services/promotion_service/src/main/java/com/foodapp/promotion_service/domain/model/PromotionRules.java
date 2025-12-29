package com.foodapp.promotion_service.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodapp.promotion_service.domain.model.recurrence.Recurrence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRules {

    @JsonProperty("schedule_rules") // time related
    private ScheduleRules scheduleRules;

    @JsonProperty("effect") // price related
    private Effect effect;

    @JsonProperty("stacking_rules") // priority related
    private StackingRules stackingRules;

    // --- Inner Static Classes ---

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleRules {
        private String timezone;
        private Recurrence recurrence;

        @JsonProperty("intraday_windows")
        private List<IntraDayWindow> intraDayWindows;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntraDayWindow {
        @JsonProperty("start_time")
        private LocalTime startTime;

        @JsonProperty("end_time")
        private LocalTime endTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Effect {
        private String type; // e.g., "PERCENTAGE_OFF_ORDER"
        private BigDecimal value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StackingRules {
        private int priority;
        private String behavior; // e.g., "EXCLUSIVE"
    }
}
