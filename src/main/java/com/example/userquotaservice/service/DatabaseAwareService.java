package com.example.userquotaservice.service;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service that helps determine the active database and execute operations accordingly
 * Acts as a dispatcher that determines whether to execute real operations or mock operations
 */
@Service
public class DatabaseAwareService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseAwareService.class);
    private static final String REAL_DB_NAME = "MySQL";
    
    private final DatabaseSelectorService databaseSelectorService;
    
    public DatabaseAwareService(DatabaseSelectorService databaseSelectorService) {
        this.databaseSelectorService = databaseSelectorService;
    }
    
    /**
     * Checks if a real database is currently active
     * 
     * @return true if a real database is active, false otherwise
     */
    public boolean isRealDatabaseActive() {
        String dbName = databaseSelectorService.getActiveProvider().getDatabaseName();
        boolean isReal = REAL_DB_NAME.equals(dbName);
        logger.debug("Current active database: {} (isReal: {})", dbName, isReal);
        return isReal;
    }
    
    /**
     * Gets the name of the currently active database
     * 
     * @return Name of the active database
     */
    public String getActiveDatabaseName() {
        return databaseSelectorService.getActiveProvider().getDatabaseName();
    }
    
    /**
     * Executes the appropriate operation based on the active database
     * If a real database is active, executes the real operation
     * Otherwise, executes the mock operation
     * 
     * @param <T> Return type of the operation
     * @param operationDescription Description of the operation for logging
     * @param realOperation Operation to execute if real database is active
     * @param mockOperation Operation to execute if mock database is active
     * @return Result of the selected operation
     */
    public <T> T executeOperation(
            String operationDescription,
            Supplier<T> realOperation,
            Supplier<T> mockOperation) {
        
        String dbName = getActiveDatabaseName();
        
        if (isRealDatabaseActive()) {
            logger.debug("Executing real operation for '{}' with database: {}", operationDescription, dbName);
            return realOperation.get();
        } else {
            logger.info("🔶 Executing mock operation for '{}' with database: {}", operationDescription, dbName);
            return mockOperation.get();
        }
    }
    
    /**
     * Validate if a resource name is valid according to the configuration
     * 
     * @param resourceName Resource name to validate
     * @param resourceOneConfigName Configured name for resource one
     * @param resourceTwoConfigName Configured name for resource two
     * @return true if valid, false otherwise
     */
    public boolean isValidResourceName(String resourceName, String resourceOneConfigName, String resourceTwoConfigName) {
        return resourceName.equals(resourceOneConfigName) || resourceName.equals(resourceTwoConfigName);
    }
} 