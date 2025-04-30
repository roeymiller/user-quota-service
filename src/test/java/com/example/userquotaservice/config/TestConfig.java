package com.example.userquotaservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.example.userquotaservice.db.DatabaseProvider;

/**
 * Test configuration class
 */
@TestConfiguration
public class TestConfig {
    
    /**
     * Creates a mock database provider for tests
     */
    @Bean
    @Primary
    public DatabaseProvider testDatabaseProvider() {
        return new DatabaseProvider() {
            @Override
            public String getDatabaseName() {
                return "TestDB";
            }
            
            @Override
            public boolean isActive() {
                return true;
            }
            
            @Override
            public void initialize() {
                // No initialization needed for tests
            }
        };
    }
} 