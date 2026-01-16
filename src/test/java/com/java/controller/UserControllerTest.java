package com.java.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.exceptions.UserException;
import com.java.model.PhoneDTO;
import com.java.model.UserDTO;
import com.java.model.UserInfo;
import com.java.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO mockUserDTO;
    private UserDTO mockUserResponseDTO;
    private UserInfo mockUserInfo;
    private UserInfo mockUserInfoResponse;

    @BeforeEach
    void setUp() {
        // Setup UserDTO for the request body
        PhoneDTO phoneDTO = new PhoneDTO();
        phoneDTO.setNumber("12345678");
        phoneDTO.setCityCode("11");
        phoneDTO.setCountryCode("57");

        mockUserDTO = new UserDTO();
        mockUserDTO.setName("New User");
        mockUserDTO.setEmail("new.user@test.com");
        mockUserDTO.setPassword("a2asfGfdfdf3");
        mockUserDTO.setPhones(List.of(phoneDTO));

        // Setup UserDTO for the service response (includes ID, token, etc.)
        mockUserResponseDTO = new UserDTO();
        mockUserResponseDTO.setId("100");
        mockUserResponseDTO.setToken("mock-jwt-token-sign-up");
        mockUserResponseDTO.setIsActive(true);
        mockUserResponseDTO.setCreated(LocalDateTime.of(2026, 1, 15, 10, 0));
        mockUserResponseDTO.setLastLogin(null);

        // Setup UserInfo for the service response for login
        UUID testValue = UUID.randomUUID();
        mockUserInfoResponse = new UserInfo();
        mockUserInfoResponse.setId(testValue);
        mockUserInfoResponse.setName("Logged In User");
        mockUserInfoResponse.setEmail("logged.in@test.com");
        mockUserInfoResponse.setToken("mock-jwt-token-login");
    }

    @Test
    @WithMockUser(username = "new.user@test.com", roles = {"USER"})
    void signUpUserHandler_Success_ShouldReturn201Created() throws Exception {
        // Arrange
        when(userService.registerUser(any(UserDTO.class))).thenReturn(mockUserResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/app/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUserDTO)).with(csrf()))
                .andExpect(status().isCreated()) // HTTP 201
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(mockUserResponseDTO.getId()))
                .andExpect(jsonPath("$.token").value(mockUserResponseDTO.getToken()))
                .andExpect(jsonPath("$.isActive").value(mockUserResponseDTO.getIsActive()));
    }

    @Test
    @WithMockUser(username = "new.user@test.com", roles = {"USER"})
    void signUpUserHandler_ServiceThrowsUserException_ShouldThrowException() throws Exception {
        // Arrange
        String exceptionMessage = "Usuario ya existe con el email: " + mockUserDTO.getEmail();
        when(userService.registerUser(any(UserDTO.class))).thenThrow(new UserException(exceptionMessage));

        // Act & Assert
        mockMvc.perform(post("/app/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUserDTO)).with(csrf()))
                .andExpect(result -> {
                    Exception rootCause = result.getResolvedException();
                    if (rootCause instanceof UserException userException) {
                        assertEquals(exceptionMessage, userException.getMessage());
                    } else {
                        throw new AssertionError("Expected UserException as the cause, but got: " + (rootCause != null ? rootCause.getClass().getSimpleName() : "null"));
                    }
                });
    }

    @Test
    @WithMockUser(username = "new.user@test.com", roles = {"USER"})
    void welcomeLoggedInUserHandler_Success_ShouldReturn200Ok() throws Exception {
        // Arrange
        when(userService.loginUser()).thenReturn(mockUserInfoResponse);

        // Act & Assert
        mockMvc.perform(get("/app/login")
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isOk()) // HTTP 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(mockUserInfoResponse.getId().toString()))
                .andExpect(jsonPath("$.email").value(mockUserInfoResponse.getEmail()))
                .andExpect(jsonPath("$.token").value(mockUserInfoResponse.getToken()));
    }

    @Test
    @WithMockUser(username = "new.user@test.com", roles = {"USER"})
    void welcomeLoggedInUserHandler_ServiceThrowsUserException_ShouldThrowException() throws Exception {
        // Arrange
        String exceptionMessage = "Error en el inicio de sesión";
        when(userService.loginUser()).thenThrow(new UserException(exceptionMessage));

        // Act & Assert
        mockMvc.perform(get("/app/login")
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(result -> {
                    Exception rootCause = result.getResolvedException();
                    if (rootCause instanceof UserException userException) {
                        assertEquals(exceptionMessage, userException.getMessage());
                    } else {
                        throw new AssertionError("Expected UserException as the cause, but got: " + (rootCause != null ? rootCause.getClass().getSimpleName() : "null"));
                    }
                });
    }
}
