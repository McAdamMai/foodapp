package com.foodapp.promotion_service;

import com.foodapp.promotion_service.api.controller.PromotionController;
import com.foodapp.promotion_service.domain.service.ActivityService;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(
        controllers = PromotionController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class, // Stop looking for a Database
                MybatisAutoConfiguration.class     // Stop looking for MyBatis Mappers
        }
)
class PromotionServiceApplicationTests {

    // You MUST mock the dependency, or the Controller cannot start
    @MockitoBean
    private ActivityService activityService;

    @Test
    void contextLoads() {
        // Now the context will successfully load
    }
}
