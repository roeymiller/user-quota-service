package com.example.userquotaservice.service;

/**
 * Common interface for all data operation services
 * Services that perform database operations should implement this interface
 */
public interface DataOperationService {
    /**
     * Method to check if the service should perform real database operations
     * This will be implemented by the proxy, not by individual services
     * 
     * @return true if real operations should be performed, false otherwise
     */
    default boolean shouldPerformRealOperations() {
        return true;
    }
} 