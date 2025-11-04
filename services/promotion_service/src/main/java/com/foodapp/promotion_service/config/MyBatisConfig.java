package com.foodapp.promotion_service.config;

import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.foodapp.promotion_service.persistence.repository")
public class MyBatisConfig {

    /**
     * Customizes MyBatis configuration.
     * - Handles NULL values properly
     * - Enables underscore-to-camelCase mapping
     */
    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return configuration -> {
            // Set JDBC type for NULL values to avoid Oracle/PostgreSQL issues
            configuration.setJdbcTypeForNull(JdbcType.NULL);

            // Enable automatic mapping from snake_case (DB) to camelCase (Java)
            configuration.setMapUnderscoreToCamelCase(true);

            // Enable lazy loading (optional, based on your needs)
            configuration.setLazyLoadingEnabled(false);
            configuration.setAggressiveLazyLoading(false);

            // Log SQL statements (useful for development)
            // configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        };
    }
}