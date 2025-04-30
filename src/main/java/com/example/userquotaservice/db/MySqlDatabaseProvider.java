package com.example.userquotaservice.db;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * MySQL database provider implementation
 * Active between configured hours (default: 00:00 to 17:59)
 */
@Component
public class MySqlDatabaseProvider implements DatabaseProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(MySqlDatabaseProvider.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final JdbcTemplate jdbcTemplate;
    
    public MySqlDatabaseProvider(
            @Value("${database.mysql.start-time:00:00}") String startTimeStr,
            @Value("${database.mysql.end-time:17:59}") String endTimeStr,
            @Autowired JdbcTemplate jdbcTemplate) {
        this.startTime = LocalTime.parse(startTimeStr, TIME_FORMATTER);
        this.endTime = LocalTime.parse(endTimeStr, TIME_FORMATTER);
        this.jdbcTemplate = jdbcTemplate;
        logger.debug("MySqlDatabaseProvider initialized with active hours: {} to {}", startTimeStr, endTimeStr);
    }
    
    @Override
    public String getDatabaseName() {
        return "MySQL";
    }
    
    @Override
    public boolean isActive() {
        LocalTime currentTime = LocalDateTime.now().toLocalTime();
        boolean isActive = !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
        if (isActive) {
            logger.debug("MySQL is currently active (current time: {})", currentTime.format(TIME_FORMATTER));
        }
        return isActive;
    }
    
    @Override
    public void initialize() {
        try {
            logger.info("Initializing MySQL database...");
            validateConnection();
            logger.info("MySQL database initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize MySQL database: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize MySQL", e);
        }
    }
    
    private void validateConnection() {
        try {
            jdbcTemplate.execute("SELECT 1");
            logger.debug("MySQL database connection validated");
        } catch (Exception e) {
            logger.error("Failed to validate MySQL connection: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to MySQL", e);
        }
    }
} 