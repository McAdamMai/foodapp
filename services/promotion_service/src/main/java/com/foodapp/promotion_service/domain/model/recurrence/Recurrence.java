package com.foodapp.promotion_service.domain.model.recurrence;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, // Use a Nickname, not the full legal name
        include = JsonTypeInfo.As.EXISTING_PROPERTY, // The label is already part of the data. Don't add extra metadata.
        property = "type",
        visible = true
)
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = DailyRecurrence.class, name = "DAILY"),
                @JsonSubTypes.Type(value = WeeklyRecurrence.class, name = "WEEKLY"),
                @JsonSubTypes.Type(value = MonthlyRecurrence.class, name = "MONTHLY"),
                @JsonSubTypes.Type(value = AnnuallyRecurrence.class, name = "ANNUALLY"),
        }
)

@Data
public abstract class Recurrence {
    public abstract String getType();
}
