package com.example.userquotaservice.db;

/**
 * Interface abstracting access to different database types
 */
public interface DatabaseProvider {
    
    /**
     * Returns the current database name
     * @return database name
     */
    String getDatabaseName();
    
    /**
     * Checks if this database is active at the current time
     * @return true if active now
     */
    boolean isActive();
    
    /**
     * Initializes the database if needed
     */
    void initialize();
} 