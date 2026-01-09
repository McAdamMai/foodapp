package com.foodapp.promotion_expander.domain.service;


import com.foodapp.promotion_expander.domain.model.IntraDayWindow;
import com.foodapp.promotion_expander.domain.model.PromotionRules;
import com.foodapp.promotion_expander.domain.model.ScheduleRule;
import com.foodapp.promotion_expander.domain.model.Recurrence;
import com.foodapp.promotion_expander.domain.model.TimeSlice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
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

        // Common Stubbing Chain
        // We use lenient() because some edge-case tests might not call all these
        lenient().when(mockRules.getSchedule()).thenReturn(mockSchedule);
        lenient().when(mockSchedule.getRecurrence()).thenReturn(mockRecurrence);

        // Default behavior: Match every date unless specified otherwise
        lenient().when(mockRecurrence.matches(any(LocalDate.class))).thenReturn(true);
        // Default Timezone
        lenient().when(mockSchedule.getTimezone()).thenReturn("UTC");
    }

    @Test
    @DisplayName("Happy Path: Should generate slices for 3 consecutive days")
    void shouldGenerateSlicesForStandardRange() {
        // Given: 3 Day Range
        Instant start = Instant.parse("2024-01-01T00:00:00Z");
        Instant end = Instant.parse("2024-01-03T23:59:59Z");

        // And: Window from 10:00 to 14:00
        LocalTime winStart = LocalTime.of(10, 0);
        LocalTime winEnd = LocalTime.of(14, 0);
        mockWindows(winStart, winEnd);

        // When
        List<TimeSlice> result = slicingEngine.expand(mockRules, start, end, null);

        // Then
        assertEquals(3, result.size(), "Should have 1 slice per day for 3 days");

        // Check first slice details
        TimeSlice firstSlice = result.get(0);
        assertEquals(Instant.parse("2024-01-01T10:00:00Z"), firstSlice.getStart());
        assertEquals(Instant.parse("2024-01-01T14:00:00Z"), firstSlice.getEnd());
    }

    @Test
    @DisplayName("Logic Check: Should skip days where Recurrence does not match")
    void shouldSkipDaysBasedOnRecurrence() {
        // Given: 3 Day Range
        Instant start = Instant.parse("2024-01-01T00:00:00Z");
        Instant end = Instant.parse("2024-01-03T23:59:59Z");
        mockWindows(LocalTime.of(10, 0), LocalTime.of(12, 0));

        // And: Recurrence only matches the middle day (Jan 2nd)
        when(mockRecurrence.matches(LocalDate.of(2024, 1, 1))).thenReturn(false);
        when(mockRecurrence.matches(LocalDate.of(2024, 1, 2))).thenReturn(true);
        when(mockRecurrence.matches(LocalDate.of(2024, 1, 3))).thenReturn(false);

        // When
        List<TimeSlice> result = slicingEngine.expand(mockRules, start, end, null);

        // Then
        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2024, 1, 2), result.get(0).getDate());
    }

    @Test
    @DisplayName("Clipping: Should clip start time if window starts before Range Start")
    void shouldClipStartTime() {
        // Given: Range starts at 11:00
        Instant rangeStart = Instant.parse("2024-01-01T11:00:00Z");
        Instant rangeEnd = Instant.parse("2024-01-01T20:00:00Z");

        // And: Window is 10:00 to 12:00 (Starts before range)
        mockWindows(LocalTime.of(10, 0), LocalTime.of(12, 0));

        // When
        List<TimeSlice> result = slicingEngine.expand(mockRules, rangeStart, rangeEnd, null);

        // Then
        assertEquals(1, result.size());
        assertEquals(rangeStart, result.get(0).getStart(), "Start should be clipped to Range Start");
        assertEquals(Instant.parse("2024-01-01T12:00:00Z"), result.get(0).getEnd(), "End should remain normal");
    }

    @Test
    @DisplayName("Clipping: Should clip end time if window ends after Range End")
    void shouldClipEndTime() {
        // Given: Range ends at 11:30
        Instant rangeStart = Instant.parse("2024-01-01T08:00:00Z");
        Instant rangeEnd = Instant.parse("2024-01-01T11:30:00Z");

        // And: Window is 10:00 to 12:00 (Ends after range)
        mockWindows(LocalTime.of(10, 0), LocalTime.of(12, 0));

        // When
        List<TimeSlice> result = slicingEngine.expand(mockRules, rangeStart, rangeEnd, null);

        // Then
        assertEquals(1, result.size());
        assertEquals(Instant.parse("2024-01-01T10:00:00Z"), result.get(0).getStart());
        assertEquals(rangeEnd, result.get(0).getEnd(), "End should be clipped to Range End");
    }

    @Test
    @DisplayName("Timezone: Should correctly handle non-UTC timezones")
    void shouldHandleTimezonesCorrectly() {
        // Given: Zone is UTC+1 (Paris winter time example)
        when(mockSchedule.getTimezone()).thenReturn("Etc/GMT-1"); // Wait, Java ZoneId sign is inverted for Etc areas, better use real region
        when(mockSchedule.getTimezone()).thenReturn("Europe/Paris");

        // Range: Jan 1st
        Instant rangeStart = Instant.parse("2024-01-01T00:00:00Z");
        Instant rangeEnd =   Instant.parse("2024-01-01T23:00:00Z");

        // Window: 10:00 Local Time
        mockWindows(LocalTime.of(10, 0), LocalTime.of(11, 0));

        // When
        List<TimeSlice> result = slicingEngine.expand(mockRules, rangeStart, rangeEnd, null);

        // Then
        // 10:00 Paris (UTC+1) is 09:00 UTC
        Instant expectedUtcStart = Instant.parse("2024-01-01T09:00:00Z");

        assertEquals(expectedUtcStart, result.get(0).getStart());
    }

    @Test
    @DisplayName("Horizon: Should respect Horizon Cap")
    void shouldRespectHorizonCap() {
        Instant rangeStart = Instant.parse("2024-01-01T00:00:00Z");
        Instant rangeEnd =   Instant.parse("2024-01-10T00:00:00Z"); // Requesting 10 days
        Instant horizonCap = Instant.parse("2024-01-02T23:59:59Z"); // Cap at 2 days

        mockWindows(LocalTime.of(10, 0), LocalTime.of(12, 0));

        List<TimeSlice> result = slicingEngine.expand(mockRules, rangeStart, rangeEnd, horizonCap);

        // Should only have Jan 1 and Jan 2
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Edge Case: Range Start is after Range End")
    void shouldThrowIfRangeInvalid() {
        Instant start = Instant.now();
        Instant end = start.minusSeconds(1);

        assertThrows(IllegalArgumentException.class, () ->
                slicingEngine.expand(mockRules, start, end, null)
        );
    }

    // --- Helper to mock the internal list of windows ---
    private void mockWindows(LocalTime start, LocalTime end) {
        IntraDayWindow window = new IntraDayWindow();
        // Assuming setters or public fields exist based on your lombok usage
        // Note: You might need to adjust this depending on if you use @Builder or setters
        window.setStartTime(start);
        window.setEndTime(end);

        when(mockSchedule.getIntradayWindows()).thenReturn(List.of(window));
    }
}
