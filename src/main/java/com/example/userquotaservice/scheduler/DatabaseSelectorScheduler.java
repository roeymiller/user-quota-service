package com.example.userquotaservice.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.userquotaservice.service.DatabaseSelectorService;

/**
 * Scheduler that periodically checks the active database
 */
@Component
public class DatabaseSelectorScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSelectorScheduler.class);
    
    private final DatabaseSelectorService databaseSelectorService;
    
    public DatabaseSelectorScheduler(DatabaseSelectorService databaseSelectorService) {
        this.databaseSelectorService = databaseSelectorService;
    }
    
    /**
     * Checks every minute if the active database needs to change
     */
    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkActiveDatabase() {
        logger.debug("Checking active database");
        databaseSelectorService.updateActiveProvider();
    }
} 