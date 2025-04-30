package com.example.userquotaservice.db;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseProvidersTest {

    @Autowired
    private List<DatabaseProvider> databaseProviders;
    
    @Autowired
    private MySqlDatabaseProvider mySqlDatabaseProvider;
    
    @Autowired
    private NotARealDbProvider notARealDbProvider;
    
    @Test
    void testDatabaseProvidersAreLoaded() {
        assertNotNull(databaseProviders);
        assertTrue(databaseProviders.size() >= 2); // Expect at least two providers
        
        // Check if all known providers are loaded
        boolean foundMySql = false;
        boolean foundNotARealDb = false;
        
        for (DatabaseProvider provider : databaseProviders) {
            if (provider.getDatabaseName().equals("MySQL")) {
                foundMySql = true;
            } else if (provider.getDatabaseName().equals("NotARealDB")) {
                foundNotARealDb = true;
            }
        }
        
        assertTrue(foundMySql, "MySQL provider not found");
        assertTrue(foundNotARealDb, "NotARealDB provider not found");
    }
    
    @Test
    void testMySqlActiveHours() {
        // Test MySQL active hours (00:00-17:59)
        boolean shouldBeActive = checkTimeInRange(LocalTime.of(10, 30), 
                                                 LocalTime.of(0, 0), 
                                                 LocalTime.of(17, 59));
        
        assertEquals(shouldBeActive, mySqlDatabaseProvider.isActive(), 
                    "MySQL should be active during 00:00-17:59");
    }
    
    @Test
    void testNotARealDbActiveHours() {
        // Test NotARealDB active hours (18:00-23:59)
        boolean shouldBeActive = checkTimeInRange(LocalTime.now(), 
                                                 LocalTime.of(18, 0), 
                                                 LocalTime.of(23, 59));
        
        assertEquals(shouldBeActive, notARealDbProvider.isActive(), 
                    "NotARealDB should be active during 18:00-23:59");
    }
    
    private boolean checkTimeInRange(LocalTime current, LocalTime start, LocalTime end) {
        return !current.isBefore(start) && !current.isAfter(end);
    }
} 