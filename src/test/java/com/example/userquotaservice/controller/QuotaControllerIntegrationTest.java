package com.example.userquotaservice.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.userquotaservice.config.TestConfig;
import com.example.userquotaservice.entity.QuotaResourceOne;
import com.example.userquotaservice.entity.QuotaResourceTwo;
import com.example.userquotaservice.entity.User;
import com.example.userquotaservice.repository.BlockedUserRepository;
import com.example.userquotaservice.repository.QuotaResourceOneRepository;
import com.example.userquotaservice.repository.QuotaResourceTwoRepository;
import com.example.userquotaservice.repository.UserRepository;

/**
 * Integration test for the Quota Controller
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
public class QuotaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlockedUserRepository blockedUserRepository;
    
    @Autowired
    private QuotaResourceOneRepository resourceOneRepository;
    
    @Autowired
    private QuotaResourceTwoRepository resourceTwoRepository;

    private User testUser;
    private int resourceOneThreshold = 3;
    private int resourceTwoThreshold = 2;

    @BeforeEach
    public void setup() {
        // Clean databases
        blockedUserRepository.deleteAll();
        userRepository.deleteAll();
        resourceOneRepository.deleteAll();
        resourceTwoRepository.deleteAll();
        
        // Create test user
        testUser = userRepository.save(new User(null, "Test", "User"));
        
        // Create resources with test thresholds
        resourceOneRepository.save(new QuotaResourceOne(null, resourceOneThreshold));
        resourceTwoRepository.save(new QuotaResourceTwo(null, resourceTwoThreshold));
    }

    @Test
    public void testConsumeQuotaOne_UnderThreshold_Returns200OK() throws Exception {
        // Send requests under threshold - should receive 200 OK
        for (int i = 0; i < resourceOneThreshold; i++) {
            mockMvc.perform(post("/v1/ConsumeQuotaResourceOne")
                    .param("userId", testUser.getId().toString())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
    
    @Test
    public void testConsumeQuotaOne_ExceedThreshold_Returns429TooManyRequests() throws Exception {
        // Send requests at threshold level - should receive 200 OK
        for (int i = 0; i < resourceOneThreshold; i++) {
            mockMvc.perform(post("/v1/ConsumeQuotaResourceOne")
                    .param("userId", testUser.getId().toString())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
        
        // Next request over threshold - should receive 429 Too Many Requests
        mockMvc.perform(post("/v1/ConsumeQuotaResourceOne")
                .param("userId", testUser.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests());
        
        // Verify user is blocked
        assertTrue(blockedUserRepository.findByUserIdAndApiName(testUser.getId(), "QuotaResourceOne").isPresent());
    }
    
    @Test
    public void testConsumeQuotaTwo_UnderThreshold_Returns200OK() throws Exception {
        // Send requests under threshold - should receive 200 OK
        for (int i = 0; i < resourceTwoThreshold; i++) {
            mockMvc.perform(post("/v1/ConsumeQuotaResourceTwo")
                    .param("userId", testUser.getId().toString())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
    
    @Test
    public void testConsumeQuotaTwo_ExceedThreshold_Returns429TooManyRequests() throws Exception {
        // Send requests at threshold level - should receive 200 OK
        for (int i = 0; i < resourceTwoThreshold; i++) {
            mockMvc.perform(post("/v1/ConsumeQuotaResourceTwo")
                    .param("userId", testUser.getId().toString())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
        
        // Next request over threshold - should receive 429 Too Many Requests
        mockMvc.perform(post("/v1/ConsumeQuotaResourceTwo")
                .param("userId", testUser.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests());
        
        // Verify user is blocked
        assertTrue(blockedUserRepository.findByUserIdAndApiName(testUser.getId(), "QuotaResourceTwo").isPresent());
    }
    
    @Test
    public void testDifferentResources_SeparateThresholds() throws Exception {
        // Test that resources don't affect each other
        
        // Use quota one up to threshold
        for (int i = 0; i < resourceOneThreshold; i++) {
            mockMvc.perform(post("/v1/ConsumeQuotaResourceOne")
                    .param("userId", testUser.getId().toString())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
        
        // Still able to use quota two
        mockMvc.perform(post("/v1/ConsumeQuotaResourceTwo")
                .param("userId", testUser.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
} 