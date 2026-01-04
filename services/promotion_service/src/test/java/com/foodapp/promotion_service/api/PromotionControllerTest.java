package com.foodapp.promotion_service.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.promotion_service.api.controller.PromotionController;
import com.foodapp.promotion_service.api.controller.dto.request.PromotionCreationDtoRequest;
import com.foodapp.promotion_service.api.controller.dto.request.PromotionUpdateRequest;
import com.foodapp.promotion_service.domain.model.PromotionDomain;
import com.foodapp.promotion_service.domain.service.ActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// I don't care what specific value is passed here
import static org.mockito.ArgumentMatchers.any;
// Normally don't need to type eq() explicitly... UNLESS you are mixing it with any()
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// Tells Spring: "Only load the Web Layer for PromotionController"
@WebMvcTest(PromotionController.class)
public class PromotionControllerTest {
    // The tool to send fake HTTP requests
    @Autowired
    private MockMvc mockMvc;

    // Helper to convert Java Objects <-> JSON Strings
    @Autowired
    private ObjectMapper objectMapper;

    // Create a Mock of the Service
    @MockitoBean
    private ActivityService activityService;

    private PromotionDomain mockDomain;

    private UUID promotionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockDomain = PromotionDomain.builder()
                .id(promotionId)
                .name("Test Promotion")
                .description("Test Promotion")
                .version(1)
                .build();  // builder is for @Builder
    }

    @Test
    void createPromotion_201_WhenValid() throws Exception {
        // record
        PromotionCreationDtoRequest request = new PromotionCreationDtoRequest(
                "Happy Hour", "Mock Description", OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(2),
                "user_1", UUID.randomUUID(), null
        );
        /*
         * If anyone calls its create() method with any 7 arguments (it doesn't matter what they are),
         * don't run the real code. Instead, just return this specific mockDomain object.
         */
        when(activityService.create(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockDomain);

        mockMvc.perform(post("/api/promotions/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createPromotion_ShouldReturn400_WhenDateIsPast() throws Exception {
        // ARRANGE: Create invalid data (Start Date is yesterday)
        PromotionCreationDtoRequest invalidRequest = new PromotionCreationDtoRequest(
                "Happy Hour",
                "Desc",
                OffsetDateTime.now().minusSeconds(86400), // <-- FAILS @Future validation
                OffsetDateTime.now().plusSeconds(3600),
                "admin",
                null,
                null
        );

        // ACT & ASSERT
        mockMvc.perform(post("/api/promotions/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Expect 400, not 201
        // Optional: Verify the error message contains the field name
        // .andExpect(jsonPath("$.startDate").exists());
    }

    @Test
    void createPromotion_ShouldReturn400_WhenNameIsNull() throws Exception {
        // ARRANGE: Create invalid data (Name is NULL)
        PromotionCreationDtoRequest invalidRequest = new PromotionCreationDtoRequest(
                null, // <-- FAILS @NotNull validation
                "Desc",
                OffsetDateTime.now().plusSeconds(3600),
                OffsetDateTime.now().plusSeconds(7200),
                "admin",
                null,
                null
        );

        // ACT & ASSERT
        mockMvc.perform(post("/api/promotions/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Expect 400
    }

    // --- TEST 2: GET ALL (GET Request) ---
    @Test
    void getAllPromotions_ShouldReturnList() throws Exception {
        // Arrange
        when(activityService.findAll()).thenReturn(List.of(mockDomain));

        // Act & Assert
        mockMvc.perform(get("/api/promotions/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1)) // The list should have 1 item
                .andExpect(jsonPath("$[0].name").value("Test Promotion"));
    }

    // --- TEST 4: UPDATE (PUT Request) ---
    @Test
    void updateDetails_ShouldReturnUpdated() throws Exception {
        PromotionUpdateRequest updateRequest = new PromotionUpdateRequest(promotionId, 2,"New Name", null, null, null, null, null, null);

        PromotionDomain updatedDomain = mockDomain.toBuilder()
                .name("New Name")
                .build();

        when(activityService.updateDetails(any(PromotionUpdateRequest.class)))
                .thenReturn(updatedDomain);

        mockMvc.perform(put("/api/promotions/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name")); // Now this passes
    }

    @Test
    void publish_ShouldCallService() throws Exception {
        when(activityService.publish(eq(promotionId), eq("publisher_user")))
                .thenReturn(mockDomain);

        mockMvc.perform(post("/api/promotions/{id}/publish", promotionId)
                .param("publishedBy", "publisher_user")) // Query Parameter
                .andExpect(status().isOk());
    }
}
