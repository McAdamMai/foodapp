package com.foodapp.promotion_expander.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleRule {
    private String timezone;
    private Recurrence recurrence;

    @JsonProperty("intraday_windows")
    private List<IntraDayWindow> intradayWindows;

}
