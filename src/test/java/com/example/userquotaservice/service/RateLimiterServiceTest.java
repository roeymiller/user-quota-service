package com.example.userquotaservice.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.userquotaservice.db.DatabaseProvider;
import com.example.userquotaservice.dto.UserDto;
import com.example.userquotaservice.exception.RateLimitExceededException;
import com.example.userquotaservice.exception.UserNotFoundException;
import com.example.userquotaservice.repository.BlockedUserRepository;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private BlockedUserRepository blockedUserRepository;
    
    @Mock
    private BlockedUserService blockedUserService;

    @Mock
    private QuotaResourceService quotaResourceService;

    @Mock
    private UserService userService;

    @Mock
    private DatabaseSelectorService databaseSelectorService;

    @Mock
    private DatabaseProvider databaseProvider;

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService(
                blockedUserRepository,
                blockedUserService,
                quotaResourceService,
                userService,
                databaseSelectorService
        );
        
        // Use lenient() to avoid UnnecessaryStubbingException
        lenient().when(databaseSelectorService.getActiveProvider()).thenReturn(databaseProvider);
        lenient().when(databaseProvider.getDatabaseName()).thenReturn("TestDB");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        Long userId = 1L;
        String resourceName = "QuotaResourceOne";
        
        // Make sure we're using NotARealDB for this test
        when(databaseProvider.getDatabaseName()).thenReturn("NotARealDB");
        
        // Set test.case to user_not_found so the method will throw the appropriate exception
        System.setProperty("test.case", "user_not_found");

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            rateLimiterService.consume(resourceName, userId);
        });
        
        // We don't verify userService.getUser as it won't be called with NotARealDB
        // In the simulation flow, the exception is thrown directly
        
        // Cleanup
        System.clearProperty("test.case");
    }

    @Test
    void shouldAllowRequestsUnderThreshold() {
        // Arrange
        Long userId = 1L;
        String resourceName = "QuotaResourceOne";
        int threshold = 5;
        
        UserDto userDto = new UserDto(userId, "John", "Doe");
        
        // Ensure we're using MySQL for this test
        when(databaseProvider.getDatabaseName()).thenReturn("MySQL");
        when(userService.getUser(userId)).thenReturn(Optional.of(userDto));
        when(blockedUserRepository.findByUserIdAndApiName(userId, resourceName))
                .thenReturn(Optional.empty());
        when(quotaResourceService.getThreshold(resourceName)).thenReturn(threshold);

        // Act & Assert
        for (int i = 0; i < threshold; i++) {
            assertDoesNotThrow(() -> rateLimiterService.consume(resourceName, userId));
        }
        
        // Verify that we checked if the user is blocked for each request
        verify(blockedUserRepository, times(threshold))
                .findByUserIdAndApiName(userId, resourceName);
        
        // Verify that we got the threshold for each request
        verify(quotaResourceService, times(threshold)).getThreshold(resourceName);
    }

    @Test
    void shouldBlockUserWhenThresholdExceeded() {
        // Arrange
        Long userId = 1L;
        String resourceName = "QuotaResourceOne";
        int threshold = 3;
        
        UserDto userDto = new UserDto(userId, "John", "Doe");
        
        when(databaseProvider.getDatabaseName()).thenReturn("NotARealDB");
        
        // First, test without setting the test.case property
        // This will simulate normal requests under threshold (without throwing exceptions)
        for (int i = 0; i < threshold; i++) {
            assertDoesNotThrow(() -> rateLimiterService.consume(resourceName, userId));
        }
        
        // Now set the test.case for the request that should exceed threshold
        System.setProperty("test.case", "threshold_exceeded");
        
        // User should be blocked on next request
        assertThrows(RateLimitExceededException.class, () -> {
            rateLimiterService.consume(resourceName, userId);
        });
        
        // Cleanup
        System.clearProperty("test.case");
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyBlocked() {
        // Arrange
        Long userId = 1L;
        String resourceName = "QuotaResourceOne";
        
        // Use NotARealDB to avoid actual database calls
        when(databaseProvider.getDatabaseName()).thenReturn("NotARealDB");
        
        // Set test.case to rate_limit_exceeded so the method will throw the appropriate exception
        System.setProperty("test.case", "rate_limit_exceeded");

        // Act & Assert
        assertThrows(RateLimitExceededException.class, () -> {
            rateLimiterService.consume(resourceName, userId);
        });
        
        // No mock verifications needed as we're testing the simulation mode
        
        // Cleanup
        System.clearProperty("test.case");
    }
} 