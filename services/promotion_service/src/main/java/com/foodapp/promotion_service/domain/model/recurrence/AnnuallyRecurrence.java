package com.foodapp.promotion_service.domain.model.recurrence;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AnnuallyRecurrence extends Recurrence{

    private final String type = "ANNUALLY";

    @NotEmpty(message = "Annual recurrence requires 'months_of_year'")
    @JsonProperty("months_of_year")
    private List<Integer> monthsOfYear;

    @NotEmpty(message = "Annual recurrence requires 'days_of_month'")
    @JsonProperty("days_of_month")
    private List<Integer> daysOfMonth;
}

