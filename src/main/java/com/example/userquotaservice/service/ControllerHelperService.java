package com.example.userquotaservice.service;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.userquotaservice.config.QuotaConfig;
import com.example.userquotaservice.exception.RateLimitExceededException;
import com.example.userquotaservice.exception.UserNotFoundException;

/**
 * Helper service for controllers that reduces duplicate code and provides shared functionality
 * such as validation, error handling, and executing operations based on the active database type
 */
@Service
public class ControllerHelperService {
    
    private static final Logger logger = LoggerFactory.getLogger(ControllerHelperService.class);
    
    private final DatabaseAwareService databaseAwareService;
    private final ResponseService responseService;
    private final QuotaConfig quotaConfig;
    
    @Autowired
    public ControllerHelperService(
            DatabaseAwareService databaseAwareService,
            ResponseService responseService,
            QuotaConfig quotaConfig) {
        this.databaseAwareService = databaseAwareService;
        this.responseService = responseService;
        this.quotaConfig = quotaConfig;
    }
    
    /**
     * Executes an operation with resource name validation
     * 
     * @param resourceName The resource name to validate
     * @param operationDescription Description of the operation (for logging)
     * @param validResourceOperation Operation to execute if resource is valid and database is real
     * @param mockOperation Operation to execute if database is mock
     * @return The result of the appropriate operation
     * @throws RateLimitExceededException if rate limit is exceeded
     * @throws UserNotFoundException if user is not found
     */
    public <T> ResponseEntity<?> executeResourceOperation(
            String resourceName,
            String operationDescription,
            Supplier<ResponseEntity<?>> validResourceOperation,
            Supplier<ResponseEntity<?>> mockOperation) throws RateLimitExceededException, UserNotFoundException {
        
        // Validate resource name
        if (!isValidResourceName(resourceName)) {
            return responseService.badRequest("Invalid Resource", "Unknown resource name: " + resourceName);
        }
        
        try {
            return databaseAwareService.executeOperation(
                operationDescription,
                validResourceOperation,
                mockOperation
            );
        } catch (RateLimitExceededException | UserNotFoundException e) {
            // Don't handle these specific exceptions here, let the controller handle them
            throw e;
        } catch (Exception e) {
            logger.error("Error in {}: {}", operationDescription, e.getMessage());
            return responseService.internalError(e.getMessage());
        }
    }
    
    /**
     * Executes a generic operation (without resource name validation)
     * 
     * @param operationDescription Description of the operation (for logging)
     * @param realOperation Operation to execute if database is real
     * @param mockOperation Operation to execute if database is mock
     * @return The result of the appropriate operation
     * @throws RateLimitExceededException if rate limit is exceeded
     * @throws UserNotFoundException if user is not found
     */
    public <T> ResponseEntity<?> executeGenericOperation(
            String operationDescription,
            Supplier<ResponseEntity<?>> realOperation,
            Supplier<ResponseEntity<?>> mockOperation) throws RateLimitExceededException, UserNotFoundException {
        
        try {
            return databaseAwareService.executeOperation(
                operationDescription,
                realOperation,
                mockOperation
            );
        } catch (RateLimitExceededException | UserNotFoundException e) {
            // Don't handle these specific exceptions here, let the controller handle them
            throw e;
        } catch (Exception e) {
            logger.error("Error in {}: {}", operationDescription, e.getMessage());
            return responseService.internalError(e.getMessage());
        }
    }
    
    /**
     * Checks if a resource name is valid
     * 
     * @param resourceName The resource name to check
     * @return true if resource is valid, false otherwise
     */
    public boolean isValidResourceName(String resourceName) {
        return databaseAwareService.isValidResourceName(
            resourceName, 
            quotaConfig.getResourceOne().getName(), 
            quotaConfig.getResourceTwo().getName()
        );
    }
} 