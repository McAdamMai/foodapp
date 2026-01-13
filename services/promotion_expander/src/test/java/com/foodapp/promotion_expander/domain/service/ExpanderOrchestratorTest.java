package com.foodapp.promotion_expander.domain.service;

import com.foodapp.promotion_expander.domain.model.ExpanderEvent;
import com.foodapp.promotion_expander.domain.model.PromotionRules;
import com.foodapp.promotion_expander.domain.model.TimeSlice;
import com.foodapp.promotion_expander.domain.model.enums.MaskType;
import com.foodapp.promotion_expander.infra.persistence.repository.ExpanderTrackerRepository;
import com.foodapp.promotion_expander.infra.persistence.repository.TimeSliceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpanderOrchestratorTest {

    @Mock
    private SlicingEngine engine;

    @Mock
    private TimeSliceRepository repo;

    @Mock
    private ExpanderTrackerRepository tracker;

    @InjectMocks
    private ExpanderOrchestrator orchestrator;

    @Test
    @DisplayName("Gatekeeper: Should IGNORE event if Tracker says version is stale (Lock Failed)")
    void shouldIgnoreEvent_WhenVersionIsStale() {
        // 1. ARRANGE
        UUID promotionId = UUID.randomUUID();
        int staleVersion = 2;

        ExpanderEvent event = mock(ExpanderEvent.class);
        when(event.getPromotionId()).thenReturn(promotionId);
        when(event.getVersion()).thenReturn(staleVersion);

        // MOCK BEHAVIOR: Tracker returns 0 (Update Failed / Lock Not Acquired)
        when(tracker.updateVersionIfNewer(promotionId, staleVersion)).thenReturn(0);

        // 2. ACT
        orchestrator.processEvent(event);

        // 3. ASSERT
        // Verify we asked the tracker
        verify(tracker).updateVersionIfNewer(promotionId, staleVersion);

        // CRITICAL: Verify the heavy processing was SKIPPED
        verifyNoInteractions(engine);
        verifyNoInteractions(repo);
    }

    @Test
    @DisplayName("Gatekeeper: Should PROCESS event if Tracker says version is new (Lock Acquired)")
    void shouldProcessEvent_WhenVersionIsFresh() {
        // 1. ARRANGE
        UUID promotionId = UUID.randomUUID();
        int newVersion = 5;

        // Create a valid "Rebuild" event
        ExpanderEvent event = createMockEvent(promotionId, newVersion);

        // MOCK BEHAVIOR: Tracker returns 1 (Update Success / Lock Acquired)
        when(tracker.updateVersionIfNewer(promotionId, newVersion)).thenReturn(1);

        // Mock engine to return empty list so we don't crash on 'domainToEntity' mapping logic
        // (We are testing the flow control, not the mapping itself)
        when(engine.expand(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        // 2. ACT
        orchestrator.processEvent(event);

        // 3. ASSERT
        // Verify lock was checked
        verify(tracker).updateVersionIfNewer(promotionId, newVersion);

        // Verify we proceeded to the Engine (The gate opened)
        verify(engine).expand(any(), any(), any(), any());

        // (Optional) Verify we attempted to delete old slices if logic dictates
        // In your current code, delete happens inside executeDelete OR executeRebuild if entities exist.
        // Since we mocked empty list, insertBatch won't be called, but the flow is confirmed.
    }

    @Test
    @DisplayName("Business Logic: Should NOT proceed to Engine when action is NO_OP (e.g., only META changes)")
    void shouldNotProceed_WhenChangeIsMetaOnly() {
        // 1. ARRANGE
        UUID promotionId = UUID.randomUUID();
        int newVersion = 6;

        // Create an event that is "Fresh" but has "Irrelevant" changes
        ExpanderEvent event = mock(ExpanderEvent.class);
        when(event.getPromotionId()).thenReturn(promotionId);
        when(event.getVersion()).thenReturn(newVersion);
        when(event.getPromotionStatus()).thenReturn("PUBLISHED");

        // Critical: The mask only contains META (e.g., someone changed the promotion description)
        when(event.getChangeMask()).thenReturn(List.of(MaskType.META));

        // Mock Tracker: The version IS new, so we pass the gatekeeper
        when(tracker.updateVersionIfNewer(promotionId, newVersion)).thenReturn(1);

        // 2. ACT
        orchestrator.processEvent(event);

        // 3. ASSERT

        // Verify we passed the version lock
        verify(tracker).updateVersionIfNewer(promotionId, newVersion);

        // Verify we calculated the action...
        // ...but verified that we DID NOT call the heavy Slicing Engine
        verifyNoInteractions(engine);

        // ...and verified we DID NOT touch the data repository
        verifyNoInteractions(repo);

        System.out.println("Test Passed: META-only change correctly triggered NO_OP");
    }

    // --- Helper to create a complex mock event ---
    private ExpanderEvent createMockEvent(UUID id, int version) {
        ExpanderEvent event = mock(ExpanderEvent.class);
        PromotionRules rules = mock(PromotionRules.class);

        when(event.getPromotionId()).thenReturn(id);
        when(event.getVersion()).thenReturn(version);
        when(event.getPromotionStatus()).thenReturn("PUBLISHED");
        when(event.getChangeMask()).thenReturn(List.of(MaskType.SCHEDULE)); // Triggers FULL_REBUILD

        // Mock timestamps for the rebuild logic
        when(event.getStartDateTime()).thenReturn(OffsetDateTime.now());
        when(event.getEndDateTime()).thenReturn(OffsetDateTime.now().plusDays(5));
        when(event.getRules()).thenReturn(rules);

        return event;
    }


}