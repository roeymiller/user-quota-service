package com.example.userquotaservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for user information
 * Allows transferring user data between application layers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    
    /**
     * User's first name - must be at least 2 characters
     */
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "First name can only contain letters, spaces, dots, and hyphens")
    private String firstName;
    
    /**
     * User's last name - must be at least 2 characters
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Last name can only contain letters, spaces, dots, and hyphens")
    private String lastName;
} 