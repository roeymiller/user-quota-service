package com.example.userquotaservice.service;

import java.util.List;

import com.example.userquotaservice.entity.BlockedUser;

/**
 * Service interface for managing blocked users
 */
public interface BlockedUserService extends DataOperationService {

    /**
     * Returns all blocked users
     */
    List<BlockedUser> getAllBlockedUsers();

    /**
     * Returns blocked users by resource
     */
    List<BlockedUser> getBlockedUsersByResource(String resourceName);
    
    /**
     * Returns all blocked entries for a specific user
     * @param userId User ID
     * @return List of blocked user entries
     */
    List<BlockedUser> getBlockedUsersByUserId(Long userId);

    /**
     * Unblocks a user for a specific resource
     * @param userId user identifier
     * @param resourceName resource identifier
     * @return true if unblocked
     */
    boolean unblockUser(Long userId, String resourceName);
} 