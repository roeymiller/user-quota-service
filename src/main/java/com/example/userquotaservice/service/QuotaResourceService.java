package com.example.userquotaservice.service;

import com.example.userquotaservice.entity.QuotaResource;

/**
 * Service interface for managing quota resources
 */
public interface QuotaResourceService extends DataOperationService {
    
    /**
     * Initializes quota resources
     */
    void init();
    
    /**
     * Returns the blocking threshold for a specific resource
     * 
     * @param resourceName the name of the resource
     * @return the threshold value
     * @throws IllegalArgumentException if resource not found
     */
    int getThreshold(String resourceName);
    
    /**
     * Updates the blocking threshold for a specific resource
     * 
     * @param resourceName the name of the resource
     * @param newThreshold the new threshold value
     * @return true if update was successful
     * @throws IllegalArgumentException if resource not found or threshold invalid
     */
    boolean updateThreshold(String resourceName, int newThreshold);
    
    /**
     * Returns details for a specific resource
     * 
     * @param resourceName the name of the resource
     * @return the resource object
     * @throws IllegalArgumentException if resource not found
     */
    QuotaResource getResource(String resourceName);
    
    /**
     * Returns the number of active users for a specific resource
     * 
     * @param resourceName the name of the resource
     * @return the count of active users
     */
    long getActiveUsersCount(String resourceName);
    
    /**
     * Returns the number of blocked users for a specific resource
     * 
     * @param resourceName the name of the resource
     * @return the count of blocked users
     */
    long getBlockedUsersCount(String resourceName);
} 