package com.foodapp.promotion_expander.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodapp.promotion_expander.domain.model.enums.RecurrenceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.Month;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Ignores fields not relevant to the specific type
public class Recurrence {

    // 1. The Discriminator (The "Switch")
    private RecurrenceType type; // Enum: DAILY, WEEKLY, MONTHLY, ANNUALLY

    // 2. Fields for WEEKLY
    @JsonProperty("days_of_week")
    private List<DayOfWeek> daysOfWeek;

    // 3. Fields for MONTHLY / ANNUALLY
    // Even if the Sender has these in separate subclasses,
    // we just list them here as nullable fields.
    @JsonProperty("day_of_month")
    private Integer dayOfMonth; // e.g., 15

    // 4. Fields for ANNUALLY
    @JsonProperty("month")
    private Month month;        // e.g., DECEMBER

    // --- Logic Helper ---
    // You move the "polymorphism" logic into this simple helper method
    public boolean matches(java.time.LocalDate date) {
        if (type == null) return false;

        return switch (type) {
            case DAILY -> true;

            case WEEKLY -> daysOfWeek != null && daysOfWeek.contains(date.getDayOfWeek());

            case MONTHLY -> dayOfMonth != null && date.getDayOfMonth() == dayOfMonth;

            case ANNUALLY -> month != null && dayOfMonth != null
                    && date.getMonth() == month
                    && date.getDayOfMonth() == dayOfMonth;
        };
    }
}