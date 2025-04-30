package com.example.userquotaservice.controller;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.userquotaservice.config.TestConfig;
import com.example.userquotaservice.entity.User;
import com.example.userquotaservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration test for the User Controller
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    @Test
    public void testCreateUser_ValidInput_ReturnsCreatedUser() throws Exception {
        // Arrange
        User user = new User(null, "John", "Doe");
        String userJson = objectMapper.writeValueAsString(user);

        // Act & Assert
        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")));

        List<User> savedUsers = userRepository.findAll();
        assertEquals(1, savedUsers.size());
        assertEquals("John", savedUsers.get(0).getFirstName());
        assertEquals("Doe", savedUsers.get(0).getLastName());
    }

    @Test
    public void testCreateUser_InvalidInput_ReturnsBadRequest() throws Exception {
        // Arrange - first name too short
        User user = new User(null, "A", "Doe");
        String userJson = objectMapper.writeValueAsString(user);

        // Act & Assert
        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").exists());

        assertEquals(0, userRepository.count());
    }

    @Test
    public void testGetAllUsers_ReturnsAllUsers() throws Exception {
        // Arrange
        userRepository.save(new User(null, "John", "Doe"));
        userRepository.save(new User(null, "Jane", "Smith"));

        // Act & Assert
        MvcResult result = mockMvc.perform(get("/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        
        // The response is a JSON object that contains a "data" field with the list of users
        Map<String, Object> responseMap = objectMapper.readValue(content, Map.class);
        List<?> usersData = (List<?>) responseMap.get("data");
        
        assertEquals(2, usersData.size());
    }
} 