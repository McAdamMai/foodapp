package com.foodapp.promotion_service.config;

<<<<<<< HEAD
import org.springframework.context.annotation.Configuration;
import org.mybatis.spring.annotation.MapperScan;

@Configuration
@MapperScan("com.foodapp.promotion_service.mapper")
public class MyBatisConfig {
=======
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.mybatis.spring.annotation.MapperScan;


import java.util.UUID;

@Configuration
@MapperScan(basePackages = "com.foodapp.promotion_service.mapper", sqlSessionFactoryRef = "sqlSessionFactory")
public class MyBatisConfig {

>>>>>>> 0ad118a (new service: promotion infra)
}
