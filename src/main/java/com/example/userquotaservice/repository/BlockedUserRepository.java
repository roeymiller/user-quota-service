package com.example.userquotaservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.userquotaservice.entity.BlockedUser;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {

    Optional<BlockedUser> findByUserIdAndApiName(Long userId, String apiName);
    
    List<BlockedUser> findAllByApiName(String apiName);
    
    /**
     * Find all blocked entries for a specific user
     * @param userId User ID
     * @return List of blocked user entries
     */
    List<BlockedUser> findAllByUserId(Long userId);
    
    /**
     * Delete all blocked entries for a specific user
     * @param userId User ID
     * @return Number of deleted entries
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM BlockedUser b WHERE b.userId = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);

    /**
     * Returns the number of blocked users for a specific API
     * @param apiName API name
     * @return number of blocked users
     */
    long countByApiName(String apiName);
}
