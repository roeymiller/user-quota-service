package com.example.userquotaservice.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userquotaservice.dto.UserDto;
import com.example.userquotaservice.entity.BlockedUser;
import com.example.userquotaservice.service.BlockedUserService;
import com.example.userquotaservice.service.ControllerHelperService;
import com.example.userquotaservice.service.ResponseService;
import com.example.userquotaservice.service.UserService;

import jakarta.validation.Valid;

/**
 * REST controller for managing users
 */
@RestController
@RequestMapping("/v1/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final ControllerHelperService controllerHelper;
    private final ResponseService responseService;
    private final BlockedUserService blockedUserService;

    public UserController(
            UserService userService, 
            ControllerHelperService controllerHelper,
            ResponseService responseService,
            BlockedUserService blockedUserService) {
        this.userService = userService;
        this.controllerHelper = controllerHelper;
        this.responseService = responseService;
        this.blockedUserService = blockedUserService;
    }

    /**
     * Create a new user
     */
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = formatValidationErrors(result);
            logger.debug("Validation errors when creating a user: {}", errors);
            return ResponseEntity.badRequest().body(errors);
        }
        
        return controllerHelper.executeGenericOperation(
            "Create new user: " + userDto.getFirstName() + " " + userDto.getLastName(),
            // Real operation with MySQL
            () -> {
                logger.info("Creating new user: {} {}", userDto.getFirstName(), userDto.getLastName());
                return ResponseEntity.ok(userService.createUser(userDto));
            },
            // Mock operation with NotARealDB
            () -> responseService.mockOperation(
                new UserDto(999L, userDto.getFirstName(), userDto.getLastName()),
                "User created successfully"
            )
        );
    }

    /**
     * Get a user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return controllerHelper.executeGenericOperation(
            "Get user with ID: " + id,
            // Real operation with MySQL
            () -> {
                logger.debug("Getting user with ID: {}", id);
                Optional<UserDto> userOpt = userService.getUser(id);
                if (userOpt.isPresent()) {
                    return responseService.success(userOpt.get(), "User retrieved successfully");
                } else {
                    return responseService.notFound("User", "User not found with id: " + id);
                }
            },
            // Mock operation with NotARealDB
            () -> {
                // If ID is very high (like 999), simulate a not found to test error handling
                if (id > 900) {
                    return responseService.notFound("User", "User not found with id: " + id);
                }
                
                // Otherwise return a mock user
                UserDto mockUser = new UserDto(id, "Mock", "User" + id);
                return responseService.mockOperation(mockUser, "User retrieved");
            }
        );
    }

    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return controllerHelper.executeGenericOperation(
            "Get all users",
            // Real operation with MySQL
            () -> {
                logger.debug("Getting all users");
                List<UserDto> users = userService.getAllUsers();
                return responseService.success(users, "Users retrieved successfully");
            },
            // Mock operation with NotARealDB
            () -> {
                // Return mock data - a small list of users
                List<UserDto> mockUsers = new ArrayList<>();
                mockUsers.add(new UserDto(1L, "Mock", "User1"));
                mockUsers.add(new UserDto(2L, "Mock", "User2"));
                mockUsers.add(new UserDto(3L, "Mock", "User3"));
                
                return responseService.mockOperation(mockUsers, "Users retrieved");
            }
        );
    }

    /**
     * Update an existing user
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto updatedUserDto, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = formatValidationErrors(result);
            logger.debug("Validation errors when updating user {}: {}", id, errors);
            return ResponseEntity.badRequest().body(errors);
        }
        
        return controllerHelper.executeGenericOperation(
            "Update user with ID: " + id,
            // Real operation with MySQL
            () -> {
                logger.info("Updating user with ID: {}", id);
                try {
                    UserDto updatedUser = userService.updateUser(id, updatedUserDto);
                    return responseService.success(updatedUser, "User updated successfully");
                } catch (RuntimeException e) {
                    return responseService.notFound("User", "User not found with id: " + id);
                }
            },
            // Mock operation with NotARealDB
            () -> {
                // If ID is very high (like 999), simulate a not found to test error handling
                if (id > 900) {
                    return responseService.notFound("User", "User not found with id: " + id);
                }
                
                // Create updated mock user
                UserDto mockUser = new UserDto(id, updatedUserDto.getFirstName(), updatedUserDto.getLastName());
                return responseService.mockOperation(mockUser, "User updated");
            }
        );
    }

    /**
     * Delete a user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return controllerHelper.executeGenericOperation(
            "Delete user with ID: " + id,
            // Real operation with MySQL
            () -> {
                logger.info("Deleting user with ID: {}", id);
                try {
                    userService.deleteUser(id);
                    return ResponseEntity.noContent().build();
                } catch (RuntimeException e) {
                    return responseService.notFound("User", "User not found with id: " + id);
                }
            },
            // Mock operation with NotARealDB
            () -> {
                // If ID is very high (like 999), simulate a not found to test error handling
                if (id > 900) {
                    return responseService.notFound("User", "User not found with id: " + id);
                }
                
                return responseService.mockOperation(null, "User deleted");
            }
        );
    }

    /**
     * Get a user's blocking status
     */
    @GetMapping("/{id}/blocked-status")
    public ResponseEntity<?> getUserBlockingStatus(@PathVariable Long id) {
        return controllerHelper.executeGenericOperation(
            "Get blocking status for user with ID: " + id,
            // Real operation with MySQL
            () -> {
                logger.debug("Getting blocking status for user with ID: {}", id);
                // First check if user exists
                Optional<UserDto> userOpt = userService.getUser(id);
                if (userOpt.isEmpty()) {
                    return responseService.notFound("User", "User not found with id: " + id);
                }
                
                // Get all blocking entries for this user
                List<BlockedUser> blockedEntries = blockedUserService.getBlockedUsersByUserId(id);
                
                Map<String, Object> response = new HashMap<>();
                response.put("userId", id);
                response.put("user", userOpt.get());
                response.put("blockedResources", blockedEntries);
                response.put("isBlocked", !blockedEntries.isEmpty());
                
                return responseService.success(response, "User blocking status retrieved successfully");
            },
            // Mock operation with NotARealDB
            () -> {
                // If ID is very high (like 999), simulate a not found to test error handling
                if (id > 900) {
                    return responseService.notFound("User", "User not found with id: " + id);
                }
                
                // Mock response
                Map<String, Object> mockResponse = new HashMap<>();
                mockResponse.put("userId", id);
                mockResponse.put("user", new UserDto(id, "Mock", "User" + id));
                mockResponse.put("blockedResources", new ArrayList<>());
                mockResponse.put("isBlocked", false);
                
                return responseService.mockOperation(mockResponse, "User blocking status retrieved");
            }
        );
    }
    
    /**
     * Process validation errors into a readable format
     */
    private Map<String, String> formatValidationErrors(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        
        for (FieldError error : result.getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        
        return errors;
    }
} 