package com.foodapp.promotion_service.domain.service;

import com.foodapp.promotion_service.config.PromotionSecurityProperties;
import com.foodapp.promotion_service.fsm.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAuthorizationService {

    private final PromotionSecurityProperties securityProperties;

    /**
     * Checks if the given userId is authorized to perform actions as the requiredRole.
     */
    public void validateUserRole(String userId, UserRole requiredRole) {
        // 1. get the map from configuration
        var mappings = securityProperties.getRoleMappings();

        // 2. Retrieve allowed users (safely handle nulls)
        List<String> authorizedUsers = mappings.getOrDefault(requiredRole, List.of());

        // 3. Check if user is in the list
        if (!authorizedUsers.contains(userId)) {
            throw new SecurityException(
                    String.format("Access Denied: User '%s' is not authorized for '%s'", userId, requiredRole)
            );
        }
    }
}
