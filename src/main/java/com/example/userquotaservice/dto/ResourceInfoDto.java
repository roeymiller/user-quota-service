package com.example.userquotaservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for quota resource information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceInfoDto {
    private String name;
    private int threshold;
    private boolean enabled;
    private long activeUsersCount;
    private long blockedUsersCount;
} 