package com.foodapp.promotion_expander;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PromotionExpanderApplication {
    public static void main(String[] args) {
        SpringApplication.run(PromotionExpanderApplication.class, args);
    }
}