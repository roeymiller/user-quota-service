package com.example.userquotaservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.userquotaservice.entity.BlockedUser;
import com.example.userquotaservice.repository.BlockedUserRepository;

/**
 * Implementation of BlockedUserService
 */
@Service
public class BlockedUserServiceImpl implements BlockedUserService {

    private final BlockedUserRepository blockedUserRepository;

    public BlockedUserServiceImpl(BlockedUserRepository blockedUserRepository) {
        this.blockedUserRepository = blockedUserRepository;
    }

    /**
     * Returns all blocked users
     */
    @Override
    public List<BlockedUser> getAllBlockedUsers() {
        return blockedUserRepository.findAll();
    }

    /**
     * Returns blocked users by resource
     */
    @Override
    public List<BlockedUser> getBlockedUsersByResource(String resourceName) {
        return blockedUserRepository.findAllByApiName(resourceName);
    }
    
    /**
     * Returns all blocked entries for a specific user
     */
    @Override
    public List<BlockedUser> getBlockedUsersByUserId(Long userId) {
        return blockedUserRepository.findAllByUserId(userId);
    }

    /**
     * Unblocks a user for a specific resource
     * @param userId user identifier
     * @param resourceName resource identifier
     * @return true if unblocked
     */
    @Override
    public boolean unblockUser(Long userId, String resourceName) {
        Optional<BlockedUser> blockedUserOpt = blockedUserRepository.findByUserIdAndApiName(userId, resourceName);
        
        if (blockedUserOpt.isPresent()) {
            blockedUserRepository.delete(blockedUserOpt.get());
            return true;
        }
        
        return false;
    }
} 