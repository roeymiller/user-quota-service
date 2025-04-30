package com.example.userquotaservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.userquotaservice.config.QuotaConfig;
import com.example.userquotaservice.dto.ResourceInfoDto;
import com.example.userquotaservice.entity.BlockedUser;
import com.example.userquotaservice.service.BlockedUserService;
import com.example.userquotaservice.service.ControllerHelperService;
import com.example.userquotaservice.service.DatabaseAwareService;
import com.example.userquotaservice.service.MockDataService;
import com.example.userquotaservice.service.QuotaResourceService;
import com.example.userquotaservice.service.ResponseService;

/**
 * Admin API interface for managing quota resources and blocked users
 */
@RestController
@RequestMapping("/v1/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final QuotaResourceService quotaResourceService;
    private final BlockedUserService blockedUserService;
    private final QuotaConfig quotaConfig;
    private final ControllerHelperService controllerHelper;
    private final ResponseService responseService;
    private final MockDataService mockDataService;

    public AdminController(
            QuotaResourceService quotaResourceService,
            BlockedUserService blockedUserService,
            QuotaConfig quotaConfig,
            DatabaseAwareService databaseAwareService,
            ResponseService responseService,
            MockDataService mockDataService,
            ControllerHelperService controllerHelper) {
        this.quotaResourceService = quotaResourceService;
        this.blockedUserService = blockedUserService;
        this.quotaConfig = quotaConfig;
        this.responseService = responseService;
        this.mockDataService = mockDataService;
        this.controllerHelper = controllerHelper;
    }

    /**
     * Gets information about all quota resources
     */
    @GetMapping("/resources")
    public ResponseEntity<?> getResourceInfo() {
        return controllerHelper.executeGenericOperation(
            "Get resource information",
            // Real operation with MySQL
            () -> {
                Map<String, Object> resourceInfo = getActualResourceInfo();
                return responseService.success(resourceInfo, "Resource information retrieved successfully");
            },
            // Mock operation with NotARealDB
            () -> responseService.mockOperation(
                mockDataService.createMockResourceInfo(),
                "Resource information retrieved"
            )
        );
    }

    /**
     * Updates the blocking threshold of a specific resource
     */
    @PutMapping("/resources/{resourceName}/threshold")
    public ResponseEntity<?> updateResourceThreshold(
            @PathVariable String resourceName,
            @RequestParam int threshold) {
        
        // Validate threshold first in all cases
        if (threshold <= 0) {
            return responseService.badRequest("Invalid Threshold", "Threshold must be greater than 0");
        }
        
        return controllerHelper.executeResourceOperation(
            resourceName,
            "Update resource threshold for " + resourceName + " to " + threshold,
            // Real operation with MySQL
            () -> {
                boolean updated = quotaResourceService.updateThreshold(resourceName, threshold);
                if (updated) {
                    return responseService.success(
                        Map.of("resourceName", resourceName, "newThreshold", threshold),
                        "Resource threshold updated successfully"
                    );
                } else {
                    return responseService.notFound("Resource", "No resource found with name: " + resourceName);
                }
            },
            // Mock operation with NotARealDB
            () -> responseService.mockOperation(
                Map.of("resourceName", resourceName, "newThreshold", threshold),
                "Resource threshold updated"
            )
        );
    }

    /**
     * Gets list of all blocked users
     */
    @GetMapping("/blocked-users")
    public ResponseEntity<?> getBlockedUsers() {
        return controllerHelper.executeGenericOperation(
            "Get all blocked users",
            // Real operation with MySQL
            () -> {
                List<BlockedUser> blockedUsers = blockedUserService.getAllBlockedUsers();
                return responseService.success(blockedUsers, "Blocked users retrieved successfully");
            },
            // Mock operation with NotARealDB
            () -> responseService.mockOperation(
                mockDataService.createAllDefaultMockBlockedUsers(),
                "Blocked users retrieved"
            )
        );
    }

    /**
     * Gets blocked users for a specific resource
     */
    @GetMapping("/blocked-users/{resourceName}")
    public ResponseEntity<?> getBlockedUsersByResource(@PathVariable String resourceName) {
        return controllerHelper.executeResourceOperation(
            resourceName,
            "Get blocked users for resource " + resourceName,
            // Real operation with MySQL
            () -> {
                List<BlockedUser> blockedUsers = blockedUserService.getBlockedUsersByResource(resourceName);
                return responseService.success(blockedUsers, "Blocked users for resource retrieved successfully");
            },
            // Mock operation with NotARealDB
            () -> {
                List<Map<String, Object>> mockBlockedUsers;
                if (resourceName.equals(quotaConfig.getResourceOne().getName())) {
                    mockBlockedUsers = mockDataService.createDefaultMockBlockedUsersForResourceOne();
                } else {
                    mockBlockedUsers = mockDataService.createDefaultMockBlockedUsersForResourceTwo();
                }
                return responseService.mockOperation(
                    mockBlockedUsers,
                    "Blocked users for resource " + resourceName + " retrieved"
                );
            }
        );
    }
    
    /**
     * Unblocks a user for a specific resource
     */
    @PutMapping("/blocked-users/{userId}/unblock")
    public ResponseEntity<?> unblockUser(
            @PathVariable Long userId, 
            @RequestParam String resourceName) {
        
        return controllerHelper.executeResourceOperation(
            resourceName,
            "Unblock user " + userId + " for resource " + resourceName,
            // Real operation with MySQL
            () -> {
                boolean unblocked = blockedUserService.unblockUser(userId, resourceName);
                if (unblocked) {
                    return responseService.success(
                        Map.of("userId", userId, "resourceName", resourceName, "unblocked", true),
                        "User " + userId + " unblocked successfully for resource " + resourceName
                    );
                } else {
                    return responseService.notFound(
                        "Block",
                        "User " + userId + " is not blocked for resource " + resourceName
                    );
                }
            },
            // Mock operation with NotARealDB
            () -> {
                // If user ID is very high, simulate that user is not blocked
                if (userId > 900) {
                    return responseService.notFound(
                        "Block",
                        "User " + userId + " is not blocked for resource " + resourceName
                    );
                }
                
                return responseService.mockOperation(
                    Map.of("userId", userId, "resourceName", resourceName, "unblocked", true),
                    "User " + userId + " unblocked for resource " + resourceName
                );
            }
        );
    }
    
    /**
     * Helper method to get resource info from the actual database
     */
    private Map<String, Object> getActualResourceInfo() {
        // Get resource information for both resources
        Map<String, Object> resourceInfo = new HashMap<>();
        
        // Information about resource type one
        String resourceOneName = quotaConfig.getResourceOne().getName();
        resourceInfo.put(resourceOneName, new ResourceInfoDto(
            resourceOneName,
            quotaConfig.getResourceOne().getThreshold(),
            quotaConfig.getResourceOne().isEnabled(),
            quotaResourceService.getActiveUsersCount(resourceOneName),
            quotaResourceService.getBlockedUsersCount(resourceOneName)
        ));
        
        // Information about resource type two
        String resourceTwoName = quotaConfig.getResourceTwo().getName();
        resourceInfo.put(resourceTwoName, new ResourceInfoDto(
            resourceTwoName,
            quotaConfig.getResourceTwo().getThreshold(),
            quotaConfig.getResourceTwo().isEnabled(),
            quotaResourceService.getActiveUsersCount(resourceTwoName),
            quotaResourceService.getBlockedUsersCount(resourceTwoName)
        ));
        
        return resourceInfo;
    }
} 