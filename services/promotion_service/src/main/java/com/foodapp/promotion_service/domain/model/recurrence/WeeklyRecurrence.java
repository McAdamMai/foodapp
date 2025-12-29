package com.foodapp.promotion_service.domain.model.recurrence;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class WeeklyRecurrence extends Recurrence {

    private final String type = "WEEKLY";

    @NotEmpty(message = "Weekly recurrence requires 'days_of_week'")
    @JsonProperty("days_of_week")
    private List<String> daysOfWeek;
}
