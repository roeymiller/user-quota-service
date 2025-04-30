package com.example.userquotaservice.util;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.example.userquotaservice.config.QuotaConfig;
import com.example.userquotaservice.entity.BlockedUser;
import com.example.userquotaservice.entity.QuotaResourceOne;
import com.example.userquotaservice.entity.QuotaResourceTwo;
import com.example.userquotaservice.entity.User;
import com.example.userquotaservice.repository.BlockedUserRepository;
import com.example.userquotaservice.repository.QuotaResourceOneRepository;
import com.example.userquotaservice.repository.QuotaResourceTwoRepository;
import com.example.userquotaservice.repository.UserRepository;

/**
 * Utility class for loading sample data into the database when the application starts
 * Only active when 'dev' profile is enabled
 */
@Configuration
@Profile("dev")
public class DataLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    private static final Random random = new Random();

    // Lists of names to create a pool of possible users
    private static final String[] FIRST_NAMES = {
        "John", "Sarah", "David", "Rachel", "Michael", "Emily", "James", "Hannah", "Jacob", "Emma",
        "Daniel", "Olivia", "Matthew", "Sophia", "Noah", "Ava", "Ethan", "Isabella", "William", "Mia"
    };
    
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
        "Wilson", "Anderson", "Taylor", "Thomas", "Moore", "Jackson", "Martin", "Lee", "Thompson", "White"
    };

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private QuotaResourceOneRepository resourceOneRepository;
    
    @Autowired
    private QuotaResourceTwoRepository resourceTwoRepository;
    
    @Autowired
    private BlockedUserRepository blockedUserRepository;
    
    @Autowired
    private QuotaConfig quotaConfig;

    /**
     * Create CommandLineRunner to load sample data
     */
    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            logger.info("Checking if sample data needs to be loaded...");
            
            if (userRepository.count() == 0) {
                logger.info("User repository is empty, loading sample data...");
                
                // Load users
                loadUsers(userRepository);
                
                // Initialize quota resources if they don't exist
                initializeQuotaResourceOne(resourceOneRepository);
                initializeQuotaResourceTwo(resourceTwoRepository);
                
                // Load blocked users
                loadBlockedUsers(blockedUserRepository, userRepository);
                
                logger.info("Sample data loading completed successfully!");
            } else {
                logger.info("User repository already contains data. Skipping sample data loading.");
            }
        };
    }
    
    /**
     * Load sample users into the database
     * Includes predefined users and random users
     */
    private void loadUsers(UserRepository userRepository) {
        // Predefined users for testing
        List<User> predefinedUsers = Arrays.asList(
                new User(null, "Jacob", "Smith"),
                new User(null, "Sarah", "Johnson"),
                new User(null, "Michael", "Williams"),
                new User(null, "Rachel", "Brown"),
                new User(null, "David", "Jones"),
                new User(null, "Emily", "Garcia"),
                new User(null, "Joseph", "Miller"),
                new User(null, "Hannah", "Davis"),
                new User(null, "Abraham", "Rodriguez"),
                new User(null, "Rebecca", "Martinez")
        );
        
        userRepository.saveAll(predefinedUsers);
        logger.info("Loaded {} predefined users", predefinedUsers.size());
        
        // Create additional random users
        int additionalUsers = 40; // Total 50 users (10 predefined + 40 random)
        User[] randomUsers = new User[additionalUsers];
        
        for (int i = 0; i < additionalUsers; i++) {
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            randomUsers[i] = new User(null, firstName, lastName);
        }
        
        userRepository.saveAll(Arrays.asList(randomUsers));
        logger.info("Loaded {} random users", additionalUsers);
        logger.info("Total {} users loaded", predefinedUsers.size() + additionalUsers);
    }
    
    private void initializeQuotaResourceOne(QuotaResourceOneRepository repository) {
        try {
            if (repository.count() == 0) {
                // Get threshold value from configuration
                int threshold = quotaConfig.getResourceOne().getThreshold();
                String resourceName = quotaConfig.getResourceOne().getName();
                
                logger.info("Creating quota resource type one ('{}') with threshold value of {}", resourceName, threshold);
                QuotaResourceOne resource = new QuotaResourceOne(null, threshold);
                repository.save(resource);
                logger.info("Quota resource type one created successfully");
            } else {
                logger.info("Quota resource type one already exists in the system");
            }
        } catch (Exception e) {
            logger.error("Error initializing quota resource type one: {}", e.getMessage());
        }
    }
    
    private void initializeQuotaResourceTwo(QuotaResourceTwoRepository repository) {
        try {
            if (repository.count() == 0) {
                // Get threshold value from configuration
                int threshold = quotaConfig.getResourceTwo().getThreshold();
                String resourceName = quotaConfig.getResourceTwo().getName();
                
                logger.info("Creating quota resource type two ('{}') with threshold value of {}", resourceName, threshold);
                QuotaResourceTwo resource = new QuotaResourceTwo(null, threshold);
                repository.save(resource);
                logger.info("Quota resource type two created successfully");
            } else {
                logger.info("Quota resource type two already exists in the system");
            }
        } catch (Exception e) {
            logger.error("Error initializing quota resource type two: {}", e.getMessage());
        }
    }
    
    private void loadBlockedUsers(BlockedUserRepository blockedUserRepository, UserRepository userRepository) {
        try {
            if (blockedUserRepository.count() == 0) {
                logger.info("Loading sample blocked user data...");
                
                // Randomly select users to block
                List<User> allUsers = userRepository.findAll();
                
                if (allUsers.size() >= 5) {  // Ensure we have enough users
                    // Block 5 random users for the first resource
                    String resourceOneName = quotaConfig.getResourceOne().getName();
                    for (int i = 0; i < 5; i++) {
                        int randomIndex = random.nextInt(allUsers.size());
                        User randomUser = allUsers.get(randomIndex);
                        
                        // Create blocking record
                        BlockedUser blockedUser = new BlockedUser();
                        blockedUser.setUserId(randomUser.getId());
                        blockedUser.setApiName(resourceOneName);
                        blockedUser.setBlockedAt(LocalDateTime.now().minusHours(random.nextInt(24))); // Random blocking time in the last 24 hours
                        
                        blockedUserRepository.save(blockedUser);
                        logger.info("User {} {} (ID: {}) blocked for resource {}", 
                                randomUser.getFirstName(), randomUser.getLastName(), randomUser.getId(), resourceOneName);
                    }
                    
                    // Block 3 random users for the second resource
                    String resourceTwoName = quotaConfig.getResourceTwo().getName();
                    for (int i = 0; i < 3; i++) {
                        int randomIndex = random.nextInt(allUsers.size());
                        User randomUser = allUsers.get(randomIndex);
                        
                        // Create blocking record
                        BlockedUser blockedUser = new BlockedUser();
                        blockedUser.setUserId(randomUser.getId());
                        blockedUser.setApiName(resourceTwoName);
                        blockedUser.setBlockedAt(LocalDateTime.now().minusHours(random.nextInt(24))); // Random blocking time in the last 24 hours
                        
                        blockedUserRepository.save(blockedUser);
                        logger.info("User {} {} (ID: {}) blocked for resource {}", 
                                randomUser.getFirstName(), randomUser.getLastName(), randomUser.getId(), resourceTwoName);
                    }
                    
                    logger.info("Total {} blocked users loaded", 8);
                } else {
                    logger.warn("Cannot load blocked users - not enough users in the system");
                }
            } else {
                logger.info("Blocked users already exist in the system. Skipping blocked user data loading.");
            }
        } catch (Exception e) {
            logger.error("Error loading blocked users: {}", e.getMessage());
        }
    }
} 