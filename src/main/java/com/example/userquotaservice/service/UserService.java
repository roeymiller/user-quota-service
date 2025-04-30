package com.example.userquotaservice.service;

import java.util.List;
import java.util.Optional;

import com.example.userquotaservice.dto.UserDto;

/**
 * Service interface for handling user-related operations
 */
public interface UserService extends DataOperationService {

    /**
     * Create a new user
     * @param userDto the user information to save
     * @return the created user DTO with ID
     */
    UserDto createUser(UserDto userDto);
    
    /**
     * Get a user by ID
     * @param id the user ID
     * @return the user DTO if found
     */
    Optional<UserDto> getUser(Long id);
    
    /**
     * Get all users
     * @return list of all user DTOs
     */
    List<UserDto> getAllUsers();
    
    /**
     * Update an existing user
     * @param id the user ID to update
     * @param updatedUserDto the new user information
     * @return the updated user DTO
     * @throws RuntimeException if user not found
     */
    UserDto updateUser(Long id, UserDto updatedUserDto);
    
    /**
     * Delete a user
     * @param id the user ID to delete
     */
    void deleteUser(Long id);
}
