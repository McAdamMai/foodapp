package com.foodapp.promotion_service.config;

import com.foodapp.promotion_service.fsm.UserRole;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.security")
public class PromotionSecurityProperties {
    // Maps the Enum (Creator) to a list of strings (user_xxx, etc.)
    private Map<UserRole, List<String>> roleMappings;
}
