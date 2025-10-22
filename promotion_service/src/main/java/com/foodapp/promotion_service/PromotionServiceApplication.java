package com.foodapp.promotion_service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.foodapp.promotion_service.mapper")
public class PromotionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PromotionServiceApplication.class, args);
    }

}
