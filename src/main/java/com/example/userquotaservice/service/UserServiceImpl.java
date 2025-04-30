package com.example.userquotaservice.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.userquotaservice.dto.UserDto;
import com.example.userquotaservice.entity.User;
import com.example.userquotaservice.repository.BlockedUserRepository;
import com.example.userquotaservice.repository.UserRepository;

/**
 * Implementation of UserService
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserRepository userRepository;
    private final BlockedUserRepository blockedUserRepository;

    public UserServiceImpl(UserRepository userRepository, BlockedUserRepository blockedUserRepository) {
        this.userRepository = userRepository;
        this.blockedUserRepository = blockedUserRepository;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = convertToEntity(userDto);
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Override
    public Optional<UserDto> getUser(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(Long id, UserDto updatedUserDto) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setFirstName(updatedUserDto.getFirstName());
                    user.setLastName(updatedUserDto.getLastName());
                    return convertToDto(userRepository.save(user));
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        // Check if user exists
        if (!userRepository.existsById(id)) {
            logger.warn("Attempted to delete non-existent user with ID: {}", id);
            return;
        }
        
        // First, delete any blocked_users entries for this user to avoid foreign key constraint violation
        int deletedBlockedEntries = blockedUserRepository.deleteAllByUserId(id);
                
        if (deletedBlockedEntries > 0) {
            logger.info("Removed {} blocked entries for user with ID: {}", deletedBlockedEntries, id);
        }
        
        // Now delete the user
        userRepository.deleteById(id);
        logger.info("User with ID: {} deleted successfully", id);
    }
    
    /**
     * Convert entity to DTO
     */
    private UserDto convertToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName()
        );
    }
    
    /**
     * Convert DTO to entity
     */
    private User convertToEntity(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getFirstName(),
                userDto.getLastName()
        );
    }
} 