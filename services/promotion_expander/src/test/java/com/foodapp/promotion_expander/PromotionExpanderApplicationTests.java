package com.foodapp.promotion_expander;

import com.foodapp.promotion_expander.domain.service.ExpanderOrchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
// This tells Spring: "Load the whole app, but pretend we don't need a Database yet"
// remove database exclusion after db is ready
// @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
class PromotionExpanderApplicationTests {

    @Autowired
    private ExpanderOrchestrator orchestrator;

    @Test
    void contextLoads() {
        // If this passes, your SlicingEngine and Orchestrator are correctly connected.
        assertThat(orchestrator).isNotNull();
    }
}