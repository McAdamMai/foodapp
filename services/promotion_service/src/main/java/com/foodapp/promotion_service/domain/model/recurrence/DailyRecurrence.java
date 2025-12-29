package com.foodapp.promotion_service.domain.model.recurrence;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DailyRecurrence extends Recurrence {
    private final String type = "DAILY";
    // No extra fields needed.
}
