package com.example.userquotaservice.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.example.userquotaservice.config.QuotaConfig;
import com.example.userquotaservice.entity.QuotaResource;
import com.example.userquotaservice.entity.QuotaResourceOne;
import com.example.userquotaservice.entity.QuotaResourceTwo;
import com.example.userquotaservice.repository.BlockedUserRepository;
import com.example.userquotaservice.repository.QuotaResourceOneRepository;
import com.example.userquotaservice.repository.QuotaResourceTwoRepository;
import com.example.userquotaservice.repository.UserRepository;

/**
 * Implementation of QuotaResourceService
 */
@Service
public class QuotaResourceServiceImpl implements QuotaResourceService {

    private static final Logger logger = LoggerFactory.getLogger(QuotaResourceServiceImpl.class);
    
    private final QuotaResourceOneRepository resourceOneRepository;
    private final QuotaResourceTwoRepository resourceTwoRepository;
    private final QuotaConfig quotaConfig;
    private final BlockedUserRepository blockedUserRepository;
    private final UserRepository userRepository;
    
    public QuotaResourceServiceImpl(QuotaResourceOneRepository resourceOneRepository, 
                                QuotaResourceTwoRepository resourceTwoRepository,
                                BlockedUserRepository blockedUserRepository,
                                UserRepository userRepository,
                                QuotaConfig quotaConfig) {
        this.resourceOneRepository = resourceOneRepository;
        this.resourceTwoRepository = resourceTwoRepository;
        this.blockedUserRepository = blockedUserRepository;
        this.userRepository = userRepository;
        this.quotaConfig = quotaConfig;
    }
    
    /**
     * Initialize data after ApplicationContext is fully initialized
     * Allows tables to be created before loading attempts
     */
    @Override
    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        logger.info("Initializing quota resources...");
        initResourceOne();
        initResourceTwo();
    }
    
    private void initResourceOne() {
        try {
            if (resourceOneRepository.count() == 0) {
                // Get threshold value from configuration
                int threshold = quotaConfig.getResourceOne().getThreshold();
                String resourceName = quotaConfig.getResourceOne().getName();
                
                logger.info("Creating resource type one with default threshold of {}", threshold);
                QuotaResourceOne resourceOne = new QuotaResourceOne(null, threshold);
                resourceOneRepository.save(resourceOne);
            } else {
                logger.info("Resource type one already exists");
            }
        } catch (Exception e) {
            logger.error("Error initializing resource type one: {}", e.getMessage());
        }
    }
    
    private void initResourceTwo() {
        try {
            if (resourceTwoRepository.count() == 0) {
                // Get threshold value from configuration
                int threshold = quotaConfig.getResourceTwo().getThreshold();
                String resourceName = quotaConfig.getResourceTwo().getName();
                
                logger.info("Creating resource type two with default threshold of {}", threshold);
                QuotaResourceTwo resourceTwo = new QuotaResourceTwo(null, threshold);
                resourceTwoRepository.save(resourceTwo);
            } else {
                logger.info("Resource type two already exists");
            }
        } catch (Exception e) {
            logger.error("Error initializing resource type two: {}", e.getMessage());
        }
    }
    
    @Override
    public int getThreshold(String resourceName) {
        // Check configuration first
        if (resourceName.equals(quotaConfig.getResourceOne().getName())) {
            return resourceOneRepository.findByResourceName(resourceName)
                    .map(QuotaResource::getBlockingThreshold)
                    .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceName));
        } else if (resourceName.equals(quotaConfig.getResourceTwo().getName())) {
            return resourceTwoRepository.findByResourceName(resourceName)
                    .map(QuotaResource::getBlockingThreshold)
                    .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceName));
        } else {
            throw new IllegalArgumentException("Unknown resource name: " + resourceName);
        }
    }
    
    @Override
    public boolean updateThreshold(String resourceName, int newThreshold) {
        if (newThreshold < 1) {
            throw new IllegalArgumentException("Threshold must be at least 1");
        }
        
        if (resourceName.equals(quotaConfig.getResourceOne().getName())) {
            return updateResourceOneThreshold(resourceName, newThreshold);
        } else if (resourceName.equals(quotaConfig.getResourceTwo().getName())) {
            return updateResourceTwoThreshold(resourceName, newThreshold);
        } else {
            throw new IllegalArgumentException("Unknown resource name: " + resourceName);
        }
    }
    
    private boolean updateResourceOneThreshold(String resourceName, int newThreshold) {
        Optional<QuotaResourceOne> resourceOpt = resourceOneRepository.findByResourceName(resourceName);
        if (resourceOpt.isPresent()) {
            QuotaResourceOne resource = resourceOpt.get();
            resource.setBlockingThreshold(newThreshold);
            resourceOneRepository.save(resource);
            return true;
        }
        return false;
    }
    
    private boolean updateResourceTwoThreshold(String resourceName, int newThreshold) {
        Optional<QuotaResourceTwo> resourceOpt = resourceTwoRepository.findByResourceName(resourceName);
        if (resourceOpt.isPresent()) {
            QuotaResourceTwo resource = resourceOpt.get();
            resource.setBlockingThreshold(newThreshold);
            resourceTwoRepository.save(resource);
            return true;
        }
        return false;
    }
    
    @Override
    public QuotaResource getResource(String resourceName) {
        if (resourceName.equals(quotaConfig.getResourceOne().getName())) {
            return resourceOneRepository.findByResourceName(resourceName)
                    .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceName));
        } else if (resourceName.equals(quotaConfig.getResourceTwo().getName())) {
            return resourceTwoRepository.findByResourceName(resourceName)
                    .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceName));
        } else {
            throw new IllegalArgumentException("Unknown resource name: " + resourceName);
        }
    }
    
    @Override
    public long getActiveUsersCount(String resourceName) {
        // Total users minus blocked users
        return userRepository.count() - getBlockedUsersCount(resourceName);
    }
    
    @Override
    public long getBlockedUsersCount(String resourceName) {
        return blockedUserRepository.countByApiName(resourceName);
    }
} 