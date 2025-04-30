package com.example.userquotaservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.userquotaservice.exception.RateLimitExceededException;
import com.example.userquotaservice.exception.UserNotFoundException;
import com.example.userquotaservice.service.ControllerHelperService;
import com.example.userquotaservice.service.RateLimiterService;
import com.example.userquotaservice.service.ResponseService;

/**
 * Controller for handling quota consumption requests
 */
@RestController
@RequestMapping("/v1")
public class QuotaController {

    private static final Logger logger = LoggerFactory.getLogger(QuotaController.class);
    private final RateLimiterService rateLimiterService;
    private final ControllerHelperService controllerHelper;
    private final ResponseService responseService;

    public QuotaController(
            RateLimiterService rateLimiterService, 
            ControllerHelperService controllerHelper,
            ResponseService responseService) {
        this.rateLimiterService = rateLimiterService;
        this.controllerHelper = controllerHelper;
        this.responseService = responseService;
    }

    /**
     * Endpoint for consuming quota resource one
     * 
     * @param userId User ID to consume the resource for
     * @return Response indicating success or failure
     */
    @PostMapping("/ConsumeQuotaResourceOne")
    public ResponseEntity<?> consumeQuotaOne(@RequestParam Long userId) {
        return consumeQuota("QuotaResourceOne", userId);
    }

    /**
     * Endpoint for consuming quota resource two
     * 
     * @param userId User ID to consume the resource for
     * @return Response indicating success or failure
     */
    @PostMapping("/ConsumeQuotaResourceTwo")
    public ResponseEntity<?> consumeQuotaTwo(@RequestParam Long userId) {
        return consumeQuota("QuotaResourceTwo", userId);
    }
    
    /**
     * Generic method to handle quota consumption for any resource
     * 
     * @param resourceName Name of the resource to consume
     * @param userId User ID to consume the resource for
     * @return Response indicating success or failure
     */
    private ResponseEntity<?> consumeQuota(String resourceName, Long userId) {
        try {
            return controllerHelper.executeResourceOperation(
                resourceName,
                "Consume " + resourceName + " for user " + userId,
                // Real operation with MySQL
                () -> {
                    rateLimiterService.consume(resourceName, userId);
                    return responseService.success(null, resourceName + " consumed successfully");
                },
                // Mock operation with NotARealDB
                () -> responseService.mockOperation(null, resourceName + " consumed successfully")
            );
        } catch (RateLimitExceededException e) {
            logger.warn("Rate limit exceeded: {}", e.getMessage());
            return responseService.error(
                HttpStatus.TOO_MANY_REQUESTS,
                "Rate Limit Exceeded", 
                e.getMessage()
            );
        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", e.getMessage());
            return responseService.notFound("User", e.getMessage());
        } catch (Exception e) {
            logger.error("Error while consuming {}", resourceName, e);
            return responseService.internalError(e.getMessage());
        }
    }
}
