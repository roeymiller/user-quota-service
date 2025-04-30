package com.example.userquotaservice.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.userquotaservice.db.DatabaseProvider;

/**
 * Service responsible for selecting the active database
 */
@Service
public class DatabaseSelectorService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSelectorService.class);
    
    private final List<DatabaseProvider> databaseProviders;
    private DatabaseProvider activeProvider;
    
    public DatabaseSelectorService(List<DatabaseProvider> databaseProviders) {
        this.databaseProviders = databaseProviders;
        updateActiveProvider();
    }
    
    /**
     * Returns the currently active database provider
     * @return active database provider
     * @throws IllegalStateException if no active provider available
     */
    public DatabaseProvider getActiveProvider() {
        if (activeProvider == null) {
            updateActiveProvider();
        }
        
        if (activeProvider == null) {
            throw new IllegalStateException("No active database provider available");
        }
        
        return activeProvider;
    }
    
    /**
     * Updates the active database provider based on current time
     */
    public void updateActiveProvider() {
        for (DatabaseProvider provider : databaseProviders) {
            if (provider.isActive()) {
                if (activeProvider != provider) {
                    logger.info("Switching to active database: {}", provider.getDatabaseName());
                    activeProvider = provider;
                    activeProvider.initialize();
                }
                return;
            }
        }
        
        logger.warn("No active database provider found for current time!");
        activeProvider = null;
    }
} 