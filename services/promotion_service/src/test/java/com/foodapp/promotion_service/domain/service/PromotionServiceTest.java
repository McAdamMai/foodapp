package com.foodapp.promotion_service.domain.service;

import com.foodapp.promotion_service.domain.event.PromotionChangedDomainEvent;
import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.domain.model.enums.AuditAction;
import com.foodapp.promotion_service.fsm.PromotionEvent;
import com.foodapp.promotion_service.fsm.PromotionStateMachine;
import com.foodapp.promotion_service.fsm.PromotionStatus;
import com.foodapp.promotion_service.fsm.UserRole;
import com.foodapp.promotion_service.persistence.entity.DayTemplateEntity;
import com.foodapp.promotion_service.persistence.entity.PromotionEntity;
import com.foodapp.promotion_service.persistence.repository.DayTemplateRepository;
import com.foodapp.promotion_service.persistence.repository.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PromotionServiceTest {

    // --- MOCKS (Dependencies)
    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private DayTemplateRepository dayTemplateRepository;

    @Mock
    private UserAuthorizationService authService;

    @Mock
    private PromotionStateMachine promotionStateMachine;

    // --- CHANGE: Mock the publisher not the Outbox
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ActivityService activityService;

    // Inspect the arguments passed to mock method
    @Captor
    private ArgumentCaptor<PromotionEntity> entityArgumentCaptor;

    @Captor
    private ArgumentCaptor<PromotionChangedDomainEvent> eventCaptor;

    // ---TEST 1: CREATE ---
    @Test
    void create_ShouldSave(){
        // Arrange
        UUID templateId = UUID.randomUUID();
        String createBy = "admin";

        // Bypass the template check
        DayTemplateEntity mockTemplate = new DayTemplateEntity();
        mockTemplate.setRuleJson(null);
        when(dayTemplateRepository.findById(templateId)).thenReturn(Optional.of(mockTemplate));

        // Act
        PromotionDomain result = activityService.create(
                "Happy Hour", "Desc", OffsetDateTime.now(), OffsetDateTime.now().plusDays(1),
                createBy, templateId, null
        );

        // Assert
        verify(promotionRepository).save(any(PromotionEntity.class));
        assertNotNull(result.getId());
        assertEquals("Happy Hour", result.getName());
    }

    // --- TEST 2: PUBLISH (The Big One) ---
    @Test
    void publish_ShouldUpdateDB_AndPublishSpringEvent(){
        // Arrange
        UUID promotionId = UUID.randomUUID();
        String user = "publisher_adam";

        // 1. Mock DB: Return an existing APPROVED promotion entity
        PromotionEntity mockEntity = PromotionEntity.builder()
                .id(promotionId)
                .status(PromotionStatus.APPROVED)
                .version(3)
                .build();

        when(promotionRepository.findById(promotionId))
                .thenReturn(mockEntity);

        // 2. Mock FSM
        when(promotionStateMachine.validateTransition(any(), eq(PromotionEvent.PUBLISH), any(), any()))
                .thenReturn(new PromotionStateMachine.TransitionResult(PromotionStatus.PUBLISHED, PromotionEvent.PUBLISH, any()));

        // 3. Mock DB Update: Simulate 1 row updated
        when(promotionRepository.updateStateTransition(any(),any(),any(),any(),any(),any()))
                .thenReturn(1);

        // Act
        PromotionDomain result = activityService.publish(promotionId, user);

        // --- ASSERTION 1: Auth Check ---
        verify(authService).validateUserRole(user, UserRole.PUBLISHER);

        // --- ASSERTION 2: DB Update ---
        verify(promotionRepository).updateStateTransition(
                eq(promotionId),
                eq(PromotionStatus.PUBLISHED),
                eq(PromotionStatus.APPROVED),
                eq(1),
                isNull(),
                eq(user)
        );

        // --- ASSERTION 3: Event Emission (The Critical Part) ---
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        PromotionChangedDomainEvent event = eventCaptor.getValue();
        assertEquals(AuditAction.PROMOTION_PUBLISH, event.getAction());
        assertEquals(user, event.getActor());
        assertEquals(PromotionStatus.APPROVED, event.getOldPromotionDomain().getStatus());
        assertEquals(PromotionStatus.PUBLISHED, event.getNewPromotionDomain().getStatus());
    }
}
