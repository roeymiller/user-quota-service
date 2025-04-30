package com.example.userquotaservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.userquotaservice.config.QuotaConfig;
import com.example.userquotaservice.dto.ResourceInfoDto;
import com.example.userquotaservice.dto.UserDto;

/**
 * Service for generating mock data when real database is not available
 * Centralizes all mock data creation logic to keep controllers clean
 */
@Service
public class MockDataService {
    
    private final Random random = new Random();
    private final QuotaConfig quotaConfig;
    
    @Autowired
    public MockDataService(QuotaConfig quotaConfig) {
        this.quotaConfig = quotaConfig;
    }
    
    /**
     * Creates a mock user with the given ID
     * 
     * @param id The user ID
     * @param firstName Optional first name (default: "Mock")
     * @param lastName Optional last name (default: "User" + id)
     * @return A mock UserDto object
     */
    public UserDto createMockUser(Long id, String firstName, String lastName) {
        if (firstName == null) {
            firstName = "Mock";
        }
        if (lastName == null) {
            lastName = "User" + id;
        }
        return new UserDto(id, firstName, lastName);
    }
    
    /**
     * Creates a list of mock users
     * 
     * @param count Number of users to create
     * @return List of mock UserDto objects
     */
    public List<UserDto> createMockUsers(int count) {
        List<UserDto> mockUsers = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            mockUsers.add(createMockUser((long)i, null, null));
        }
        return mockUsers;
    }
    
    /**
     * Creates a mock blocked user entry
     * 
     * @param userId User ID to block
     * @param resourceName Resource name for which the user is blocked
     * @return A map representing a mock blocked user
     */
    public Map<String, Object> createMockBlockedUser(Long userId, String resourceName) {
        Map<String, Object> blockedUser = new HashMap<>();
        blockedUser.put("id", (long)(random.nextInt(1000)));
        blockedUser.put("userId", userId);
        blockedUser.put("apiName", resourceName);
        blockedUser.put("blockedAt", LocalDateTime.now().minusHours(random.nextInt(24)).toString());
        return blockedUser;
    }
    
    /**
     * Creates a list of mock blocked users for a specific resource
     * 
     * @param resourceName Resource name
     * @param userIds List of user IDs to include
     * @return List of mock blocked user objects
     */
    public List<Map<String, Object>> createMockBlockedUsers(String resourceName, List<Long> userIds) {
        List<Map<String, Object>> mockBlockedUsers = new ArrayList<>();
        for (Long userId : userIds) {
            mockBlockedUsers.add(createMockBlockedUser(userId, resourceName));
        }
        return mockBlockedUsers;
    }
    
    /**
     * Creates a mock resource info for resource one
     * 
     * @return Mock ResourceInfoDto for resource one
     */
    public ResourceInfoDto createMockResourceOneInfo() {
        String resourceName = quotaConfig.getResourceOne().getName();
        return new ResourceInfoDto(
            resourceName,
            quotaConfig.getResourceOne().getThreshold(),
            quotaConfig.getResourceOne().isEnabled(),
            8, // Mock active users
            2  // Mock blocked users
        );
    }
    
    /**
     * Creates a mock resource info for resource two
     * 
     * @return Mock ResourceInfoDto for resource two
     */
    public ResourceInfoDto createMockResourceTwoInfo() {
        String resourceName = quotaConfig.getResourceTwo().getName();
        return new ResourceInfoDto(
            resourceName,
            quotaConfig.getResourceTwo().getThreshold(),
            quotaConfig.getResourceTwo().isEnabled(),
            6, // Mock active users
            3  // Mock blocked users
        );
    }
    
    /**
     * Creates a map of mock resource info for all resources
     * 
     * @return Map of resource name to mock resource info
     */
    public Map<String, ResourceInfoDto> createMockResourceInfo() {
        Map<String, ResourceInfoDto> mockResourceInfo = new HashMap<>();
        mockResourceInfo.put(quotaConfig.getResourceOne().getName(), createMockResourceOneInfo());
        mockResourceInfo.put(quotaConfig.getResourceTwo().getName(), createMockResourceTwoInfo());
        return mockResourceInfo;
    }
    
    /**
     * Creates default mock blocked users for resource one
     * 
     * @return List of mock blocked users
     */
    public List<Map<String, Object>> createDefaultMockBlockedUsersForResourceOne() {
        return createMockBlockedUsers(quotaConfig.getResourceOne().getName(), List.of(1L, 3L));
    }
    
    /**
     * Creates default mock blocked users for resource two
     * 
     * @return List of mock blocked users
     */
    public List<Map<String, Object>> createDefaultMockBlockedUsersForResourceTwo() {
        return createMockBlockedUsers(quotaConfig.getResourceTwo().getName(), List.of(2L, 4L, 5L));
    }
    
    /**
     * Creates a list of all default mock blocked users across all resources
     * 
     * @return List of mock blocked users
     */
    public List<Map<String, Object>> createAllDefaultMockBlockedUsers() {
        List<Map<String, Object>> allBlockedUsers = new ArrayList<>();
        allBlockedUsers.addAll(createDefaultMockBlockedUsersForResourceOne());
        allBlockedUsers.addAll(createDefaultMockBlockedUsersForResourceTwo());
        return allBlockedUsers;
    }
} 