package com.foodapp.promotion_service.config;

import org.springframework.context.annotation.Configuration;
import org.mybatis.spring.annotation.MapperScan;

@Configuration
@MapperScan("com.foodapp.promotion_service.mapper")
public class MyBatisConfig {
}
