package com.foodapp.promotion_expander.domain.service;

import com.foodapp.promotion_expander.domain.model.IntraDayWindow;
import com.foodapp.promotion_expander.domain.model.PromotionRules;
import com.foodapp.promotion_expander.domain.model.TimeSlice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
public class SlicingEngine {

    List<TimeSlice> expand(PromotionRules rules, LocalDate rangeStart, LocalDate rangeEnd, Instant clipStart, Instant clipEnd) {

        Objects.requireNonNull(rules, "PromotionRules cannot be null");
        Objects.requireNonNull(rules.getSchedule(), "Schedule cannot be null");
        Objects.requireNonNull(rangeStart, "rangeStart cannot be null");
        Objects.requireNonNull(rangeEnd, "rangeEnd cannot be null");

        if (rangeStart.isAfter(rangeEnd)) {
            throw new IllegalArgumentException("rangeStart cannot be after rangeEnd");
        }

        // initialize res
        List<TimeSlice> timeSlices = new ArrayList<>();

        // attempt get timezone
        ZoneId zone;
        String zoneInRules = rules.getSchedule().getTimezone();
        try {
            zone = ZoneId.of(zoneInRules);
        } catch (Exception e){
            log.error("Invalid timezone" + zoneInRules);
            throw new IllegalArgumentException("Invalid timezone in schedule", e);
        }


        // iterate over each day
        for (LocalDate date = rangeStart; !date.isAfter(rangeEnd); date = date.plusDays(1)) {

            // recurrence check
            if (!rules.getSchedule().getRecurrence().matches(date)) {
                log.info("Skipping date to recurrence: " + date);
                continue;
            }

            // expander the windows
            // iterate over the windows
            List<IntraDayWindow> windows = rules.getSchedule().getIntradayWindows();
            if (windows == null || windows.isEmpty()) {
                log.warn("No intraday windows defined for promotion");
                return Collections.emptyList();
            }
            for(IntraDayWindow window : windows) {
                if (window.getStartTime() == null || window.getEndTime() == null) {
                    log.warn("Skipping window with null start/end times on date: " + date);
                    continue;
                }

                if (window.getStartTime().isAfter(window.getEndTime())) {
                    log.warn("Skipping window with start/end times (start >= end) on date: " + date);
                    continue;
                }

                ZonedDateTime zdtStart = ZonedDateTime.of(date, window.getStartTime(), zone);
                ZonedDateTime zdtEnd = ZonedDateTime.of(date, window.getEndTime(), zone);

                Instant sliceStart = zdtStart.toInstant();
                Instant sliceEnd = zdtEnd.toInstant();

                if(sliceEnd.isBefore(clipStart) || sliceStart.isAfter(clipEnd)) {continue;}
                // what if the time sit b/w rangeEnd and clipEnd?
                if(sliceStart.isBefore(clipStart)) {sliceStart = clipStart;}
                if(sliceEnd.isAfter(clipEnd)) {sliceEnd = clipEnd;}

                if (!sliceStart.isBefore(sliceEnd)) continue;

                TimeSlice timeSlice = TimeSlice.builder()
                        .date(date)
                        .start(sliceStart)
                        .end(sliceEnd)
                        .promotionRules(rules)
                        .build();
                timeSlices.add(timeSlice);
            }
        }
        log.info("Generated " + timeSlices.size() + " time slices");
        return timeSlices;
    }
}
