package com.foodapp.promotion_expander.domain.service;

import com.foodapp.promotion_expander.domain.model.ExpanderEvent;
import com.foodapp.promotion_expander.domain.model.PromotionRules;
import com.foodapp.promotion_expander.domain.model.enums.MaskType;
import com.foodapp.promotion_expander.infra.persistence.repository.ExpanderTrackerRepository;
import com.foodapp.promotion_expander.infra.persistence.repository.TimeSliceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpanderOrchestratorTest {

    @Mock private SlicingEngine engine;
    @Mock private TimeSliceRepository repo;
    @Mock private ExpanderTrackerRepository tracker;

    @InjectMocks
    private ExpanderOrchestrator orchestrator;

    @Test
    @DisplayName("Gatekeeper: Should IGNORE event if Tracker returns 0 (Stale Version)")
    void shouldIgnoreEvent_WhenVersionIsStale() {
        // Arrange
        ExpanderEvent event = createMockEvent(UUID.randomUUID(), 2, "PUBLISHED", List.of(MaskType.SCHEDULE));

        // Mock Tracker returning 0 (Failed update)
        when(tracker.updateVersionIfNewer(any())).thenReturn(0);

        // Act
        orchestrator.processEvent(event);

        // Assert
        verify(tracker).updateVersionIfNewer(any());
        verifyNoInteractions(engine);
        verifyNoInteractions(repo);
    }

    @Test
    @DisplayName("Rebuild: Should apply Buffer Strategy (+/- 1 Day) when calling Engine")
    void shouldApplyBufferStrategy() {
        // Arrange
        // In ExpanderOrchestratorTest.java inside shouldApplyBufferStrategy()

        ReflectionTestUtils.setField(orchestrator, "horizontalDays", 30);
        ReflectionTestUtils.setField(orchestrator, "batchSize", 100);

// ADD THIS LINE
        ReflectionTestUtils.setField(orchestrator, "TIMEZONE_BUFFER_DAYS", 1);

        UUID promoId = UUID.randomUUID();

        // Dates: Jan 20 to Jan 25 (UTC)
        OffsetDateTime start = OffsetDateTime.of(2026, 1, 20, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime end = OffsetDateTime.of(2026, 1, 25, 0, 0, 0, 0, ZoneOffset.UTC);

        ExpanderEvent event = createMockEvent(promoId, 5, "PUBLISHED", List.of(MaskType.SCHEDULE));
        when(event.getStartDateTime()).thenReturn(start);
        when(event.getEndDateTime()).thenReturn(end);

        // Tracker Success
        when(tracker.updateVersionIfNewer(any())).thenReturn(1);
        // Engine Success
        when(engine.expand(any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());

        // Act
        orchestrator.processEvent(event);

        // Assert
        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);

        verify(engine).expand(
                any(),
                startCaptor.capture(),
                endCaptor.capture(),
                any(),
                any()
        );

        // Verify Buffer Logic:
        // Tracker (UTC) = Jan 20. Engine (Buffer) = Jan 19.
        assertEquals(LocalDate.of(2026, 1, 19), startCaptor.getValue());

        // Tracker (UTC) = Jan 25. Engine (Buffer) = Jan 26.
        assertEquals(LocalDate.of(2026, 1, 26), endCaptor.getValue());
    }

    @Test
    @DisplayName("Future Promo: Should defer expansion if Start Date > Horizon")
    void shouldDeferExpansion_WhenFuturePromo() {
        ReflectionTestUtils.setField(orchestrator, "horizontalDays", 30);

        // Start Date is 60 days in future
        OffsetDateTime futureStart = OffsetDateTime.now(ZoneOffset.UTC).plusDays(60);
        ExpanderEvent event = createMockEvent(UUID.randomUUID(), 1, "PUBLISHED", List.of(MaskType.SCHEDULE));
        when(event.getStartDateTime()).thenReturn(futureStart);
        when(event.getEndDateTime()).thenReturn(futureStart.plusDays(5));

        when(tracker.updateVersionIfNewer(any())).thenReturn(1);

        // Act
        orchestrator.processEvent(event);

        // Assert
        verify(repo).deleteSlicesByPromotionId(event.getPromotionId());
        verifyNoInteractions(engine);
    }

    // Helper
    private ExpanderEvent createMockEvent(UUID id, int version, String status, List<MaskType> mask) {
        ExpanderEvent event = mock(ExpanderEvent.class);
        PromotionRules rules = mock(PromotionRules.class);

        // Mock internal structure loosely using lenient in case they aren't called
        lenient().when(event.getPromotionId()).thenReturn(id);
        lenient().when(event.getVersion()).thenReturn(version);
        lenient().when(event.getPromotionStatus()).thenReturn(status);
        lenient().when(event.getChangeMask()).thenReturn(mask);
        lenient().when(event.getRules()).thenReturn(rules);

        lenient().when(event.getStartDateTime()).thenReturn(OffsetDateTime.now(ZoneOffset.UTC));
        lenient().when(event.getEndDateTime()).thenReturn(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));

        return event;
    }
}