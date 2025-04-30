package com.example.userquotaservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Central configuration for API quotas
 * Centralizes all threshold settings and quotas in one place
 */
@Configuration
@ConfigurationProperties(prefix = "app.quota")
@Validated
@Data
public class QuotaConfig {

    private static final Logger logger = LoggerFactory.getLogger(QuotaConfig.class);

    /**
     * Settings for quota resource type one
     */
    private ResourceOne resourceOne = new ResourceOne();
    
    /**
     * Settings for quota resource type two
     */
    private ResourceTwo resourceTwo = new ResourceTwo();
    
    /**
     * Inner class for type one quota resource settings
     */
    @Data
    public static class ResourceOne {
        /**
         * Maximum request threshold before blocking
         */
        @Min(1)
        private int threshold = 5;
        
        /**
         * Resource name as it appears in the system
         */
        private String name = "QuotaResourceOne";
        
        /**
         * Whether this resource is active in the system
         */
        private boolean enabled = true;
    }
    
    /**
     * Inner class for type two quota resource settings
     */
    @Data
    public static class ResourceTwo {
        /**
         * Maximum request threshold before blocking
         */
        @Min(1)
        private int threshold = 3;
        
        /**
         * Resource name as it appears in the system
         */
        private String name = "QuotaResourceTwo";
        
        /**
         * Whether this resource is active in the system
         */
        private boolean enabled = true;
    }
} 