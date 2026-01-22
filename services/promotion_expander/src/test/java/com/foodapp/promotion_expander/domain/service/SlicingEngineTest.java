package com.foodapp.promotion_expander.domain.service;

import com.foodapp.promotion_expander.domain.model.IntraDayWindow;
import com.foodapp.promotion_expander.domain.model.PromotionRules;
import com.foodapp.promotion_expander.domain.model.Recurrence;
import com.foodapp.promotion_expander.domain.model.ScheduleRule;
import com.foodapp.promotion_expander.domain.model.TimeSlice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlicingEngineTest {

    private SlicingEngine slicingEngine;

    @Mock
    private PromotionRules mockRules;
    @Mock
    private ScheduleRule mockSchedule;
    @Mock
    private Recurrence mockRecurrence;

    @BeforeEach
    void setUp() {
        slicingEngine = new SlicingEngine();

        // Common Stubbing
        lenient().when(mockRules.getSchedule()).thenReturn(mockSchedule);
        lenient().when(mockSchedule.getRecurrence()).thenReturn(mockRecurrence);

        // Default: Recurrence matches every date
        lenient().when(mockRecurrence.matches(any(LocalDate.class))).thenReturn(true);
        // Default: UTC timezone
        lenient().when(mockSchedule.getTimezone()).thenReturn("UTC");
    }

    @Test
    @DisplayName("Happy Path: Should generate slices for 3 consecutive days")
    void shouldGenerateSlicesForStandardRange() {
        // Given: Jan 1 to Jan 3
        LocalDate queryStart = LocalDate.of(2024, 1, 1);
        LocalDate queryEnd = LocalDate.of(2024, 1, 3);

        // Clipping bounds (Full Range)
        Instant clipStart = Instant.parse("2024-01-01T00:00:00Z");
        Instant clipEnd = Instant.parse("2024-01-03T23:59:59Z");

        // Window: 10:00 - 14:00
        mockWindows(LocalTime.of(10, 0), LocalTime.of(14, 0));

        // When
        List<TimeSlice> result = slicingEngine.expand(mockRules, queryStart, queryEnd, clipStart, clipEnd);

        // Then
        assertEquals(3, result.size());
        assertEquals(Instant.parse("2024-01-01T10:00:00Z"), result.get(0).getStart());
        assertEquals(Instant.parse("2024-01-01T14:00:00Z"), result.get(0).getEnd());
    }

    @Test
    @DisplayName("Recurrence: Should skip days that don't match")
    void shouldSkipDaysBasedOnRecurrence() {
        LocalDate queryStart = LocalDate.of(2024, 1, 1);
        LocalDate queryEnd = LocalDate.of(2024, 1, 3);

        // Only Jan 2 matches
        when(mockRecurrence.matches(LocalDate.of(2024, 1, 1))).thenReturn(false);
        when(mockRecurrence.matches(LocalDate.of(2024, 1, 2))).thenReturn(true);
        when(mockRecurrence.matches(LocalDate.of(2024, 1, 3))).thenReturn(false);

        mockWindows(LocalTime.of(10, 0), LocalTime.of(12, 0));

        // When
        List<TimeSlice> result = slicingEngine.expand(mockRules, queryStart, queryEnd, Instant.MIN, Instant.MAX);

        // Then
        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2024, 1, 2), result.get(0).getDate());
    }

    @Test
    @DisplayName("Clipping: Should trim start/end if outside absolute bounds")
    void shouldClipSlices() {
        LocalDate queryDate = LocalDate.of(2024, 1, 1);

        // Window: 10:00 to 14:00
        mockWindows(LocalTime.of(10, 0), LocalTime.of(14, 0));

        // Scenario: Promo starts 11:00, ends 13:00
        Instant clipStart = Instant.parse("2024-01-01T11:00:00Z");
        Instant clipEnd = Instant.parse("2024-01-01T13:00:00Z");

        // When
        List<TimeSlice> result = slicingEngine.expand(mockRules, queryDate, queryDate, clipStart, clipEnd);

        // Then
        assertEquals(1, result.size());
        assertEquals(clipStart, result.get(0).getStart());
        assertEquals(clipEnd, result.get(0).getEnd());
    }

    @Test
    @DisplayName("Clipping: Should ignore slices completely outside bounds")
    void shouldIgnoreSlicesOutsideBounds() {
        LocalDate queryDate = LocalDate.of(2024, 1, 1);
        mockWindows(LocalTime.of(10, 0), LocalTime.of(14, 0));

        // Scenario: Promo ended Yesterday
        Instant clipEnd = Instant.parse("2023-12-31T23:59:59Z");

        // When
        List<TimeSlice> result = slicingEngine.expand(mockRules, queryDate, queryDate, Instant.MIN, clipEnd);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Timezone: Should convert Local Time to correct UTC Instant")
    void shouldHandleTimezones() {
        // Zone: Paris (UTC+1 in Winter)
        when(mockSchedule.getTimezone()).thenReturn("Europe/Paris");

        LocalDate queryDate = LocalDate.of(2024, 1, 1);

        // Window: 10:00 Local Paris Time
        mockWindows(LocalTime.of(10, 0), LocalTime.of(11, 0));

        // When
        List<TimeSlice> result = slicingEngine.expand(mockRules, queryDate, queryDate, Instant.MIN, Instant.MAX);

        // Then: 10:00 Paris = 09:00 UTC
        Instant expectedUtc = Instant.parse("2024-01-01T09:00:00Z");
        assertEquals(expectedUtc, result.get(0).getStart());
    }

    // Helper method defined INSIDE the class
    private void mockWindows(LocalTime start, LocalTime end) {
        IntraDayWindow window = new IntraDayWindow();
        window.setStartTime(start);
        window.setEndTime(end);
        when(mockSchedule.getIntradayWindows()).thenReturn(List.of(window));
    }
}