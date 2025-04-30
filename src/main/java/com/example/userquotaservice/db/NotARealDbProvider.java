package com.example.userquotaservice.db;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * NotARealDB database provider implementation (mock)
 * Active between configured hours (default: 18:00 to 23:59)
 */
@Component
public class NotARealDbProvider implements DatabaseProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(NotARealDbProvider.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    private final LocalTime startTime;
    private final LocalTime endTime;
    

    public NotARealDbProvider(
            @Value("${database.notarealdb.start-time:18:00}") String startTimeStr,
            @Value("${database.notarealdb.end-time:23:59}") String endTimeStr) {
        this.startTime = LocalTime.parse(startTimeStr, TIME_FORMATTER);
        this.endTime = LocalTime.parse(endTimeStr, TIME_FORMATTER);
        logger.debug("NotARealDbProvider initialized with active hours: {} to {}", startTimeStr, endTimeStr);
    }
    

    public String getName() {
        return "notarealdb";
    }
    

    @Override
    public String getDatabaseName() {
        return "NotARealDB";
    }
    

    @Override
    public boolean isActive() {
        LocalTime currentTime = LocalDateTime.now().toLocalTime();
        boolean isActive;
        if (startTime.isAfter(endTime)) {
            isActive = !currentTime.isBefore(startTime) || !currentTime.isAfter(endTime);
        } else {
            isActive = !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
        }
        
        if (isActive) {
            logger.debug("NotARealDB is currently active (current time: {})", currentTime.format(TIME_FORMATTER));
        }
        return isActive;
    }
    
    /**
     * Initializes the database connection
     * This is a mock implementation that only prints to console
     */
    @Override
    public void initialize() {
        logger.info("NotARealDB initialized");
    }
} 