package com.example.userquotaservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.example.userquotaservice.proxy.DataServiceProxyFactory;
import com.example.userquotaservice.service.BlockedUserService;
import com.example.userquotaservice.service.BlockedUserServiceImpl;
import com.example.userquotaservice.service.QuotaResourceService;
import com.example.userquotaservice.service.RateLimiterService;
import com.example.userquotaservice.service.UserService;

/**
 * Configuration class for creating service proxies
 * These proxies will intercept calls to database services and decide whether to
 * execute them or return mock responses based on the active database provider.
 */
@Configuration
public class ServiceProxyConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceProxyConfiguration.class);
    
    @Autowired
    private DataServiceProxyFactory proxyFactory;
    
    public ServiceProxyConfiguration() {
        logger.info("Initializing ServiceProxyConfiguration");
    }
    
    /**
     * Create a proxy for UserService
     */
    @Bean
    @Primary
    public UserService userServiceProxy(UserService userService) {
        logger.info("Creating proxy for UserService");
        return proxyFactory.createServiceProxy(userService, UserService.class);
    }
    
    /**
     * Create a proxy for QuotaResourceService
     */
    @Bean
    @Primary
    public QuotaResourceService quotaResourceServiceProxy(QuotaResourceService quotaResourceService) {
        logger.info("Creating proxy for QuotaResourceService");
        return proxyFactory.createServiceProxy(quotaResourceService, QuotaResourceService.class);
    }
    
    /**
     * Create a proxy for BlockedUserService
     */
    @Bean
    @Primary
    public BlockedUserService blockedUserServiceProxy(BlockedUserServiceImpl blockedUserService) {
        logger.info("Creating proxy for BlockedUserService");
        return proxyFactory.createServiceProxy(blockedUserService, BlockedUserService.class);
    }
    
    /**
     * Create a proxy for RateLimiterService
     * We skip proxy creation for this service because it's not an interface implementation
     * and directly uses other services
     */
    @Bean
    @Primary
    public RateLimiterService rateLimiterServiceProxy(RateLimiterService rateLimiterService) {
        logger.info("Creating direct reference for RateLimiterService (no proxy needed)");
        return rateLimiterService;
    }
} 