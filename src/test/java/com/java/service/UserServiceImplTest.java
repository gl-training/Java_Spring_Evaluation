package com.java.service;


import com.java.exceptions.UserException;
import com.java.model.PhoneDTO;
import com.java.model.UserDTO;
import com.java.model.UserInfo;
import com.java.repository.UserRepository;
import com.java.utils.EncryptionUtil;
import com.java.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EncryptionUtil encryptionUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDTO mockUserDTO;
    private UserInfo mockUserInfo;

    @BeforeEach
    void setUp() {
        // Setup a UserDTO for registration
        PhoneDTO phoneDTO = new PhoneDTO();
        phoneDTO.setNumber("12345678");
        phoneDTO.setCityCode("11");
        phoneDTO.setCountryCode("57");

        mockUserDTO = new UserDTO();
        mockUserDTO.setName("Test User");
        mockUserDTO.setEmail("test@example.com");
        mockUserDTO.setPassword("rawPassword123");
        mockUserDTO.setPhones(List.of(phoneDTO));

        // Setup a UserInfo for successful save/login
        mockUserInfo = new UserInfo();
        mockUserInfo.setId(UUID.randomUUID());
        mockUserInfo.setName("Test User");
        mockUserInfo.setEmail("test@example.com");
        mockUserInfo.setPassword("encrypted:rawPassword123");
        mockUserInfo.setToken("someInitialToken");
        mockUserInfo.setIsActive(false);
        mockUserInfo.setCreated(LocalDateTime.of(2025, 1, 1, 10, 0));
        mockUserInfo.setLastLogin(null);
        mockUserInfo.setPhones(new ArrayList<>()); // Simplified phone list
    }

    @Test
    void registerUser_Success() throws Exception {
        UUID testValue = UUID.randomUUID();
        // Arrange
        when(userRepo.findByEmail(anyString())).thenReturn(null);
        when(encryptionUtil.encrypt(anyString())).thenReturn("encryptedPassword");
        when(jwtUtil.generateToken(anyString())).thenReturn("newUserToken");

        when(userRepo.save(any(UserInfo.class))).thenAnswer(invocation -> {
            UserInfo savedUser = invocation.getArgument(0);
            savedUser.setId(testValue); // Simulate ID generation upon save
            savedUser.setCreated(LocalDateTime.of(2026, 1, 1, 10, 0)); // Simulate creation date setting
            savedUser.setLastLogin(null);
            return savedUser;
        });

        // Act
        UserDTO result = userService.registerUser(mockUserDTO);

        // Assert
        // Verify repository methods were called
        verify(userRepo, times(1)).findByEmail(mockUserDTO.getEmail());
        verify(userRepo, times(1)).save(any(UserInfo.class));

        // Verify the result DTO is correct
        assertNotNull(result);
        assertEquals(testValue.toString(), result.getId());
        assertEquals("newUserToken", result.getToken());
        assertEquals(true, result.getIsActive());

        // Verify that the saved UserInfo had the correct values
        verify(userRepo).save(any(UserInfo.class));
    }

    @Test
    void registerUser_UserAlreadyExists_ThrowsUserException() {
        // Arrange
        // 1. Mock: userRepo.findByEmail returns a UserInfo (user already exists)
        when(userRepo.findByEmail(anyString())).thenReturn(mockUserInfo);

        // Act & Assert
        UserException thrown = assertThrows(UserException.class, () -> {
            userService.registerUser(mockUserDTO);
        });

        // Verify the exception message (Spanish as in the original code)
        assertEquals("User already exist with email: " + mockUserDTO.getEmail(), thrown.getMessage());

        // Verify that save, encrypt, and token generation were NEVER called
        verify(userRepo, times(1)).findByEmail(mockUserDTO.getEmail());
        verify(userRepo, never()).save(any(UserInfo.class));
        verify(encryptionUtil, never()).encrypt(anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void loginUser_Success() {
        // Arrange
        String userEmail = mockUserDTO.getEmail();
        String newToken = "newlyGeneratedToken";
        LocalDateTime now = LocalDateTime.now();

        // 1. Mock dependencies
        when(userRepo.findByEmail(userEmail)).thenReturn(mockUserInfo);
        when(jwtUtil.generateToken(userEmail)).thenReturn(newToken);

        // 2. Mock userRepo.save to update the token/date and return the updated object
        when(userRepo.save(any(UserInfo.class))).thenAnswer(invocation -> {
            UserInfo savedUser = invocation.getArgument(0);
            savedUser.setToken(newToken);
            return savedUser;
        });

        // 3. Mock EncryptionUtil.decrypt
        when(encryptionUtil.decrypt(mockUserInfo.getPassword())).thenReturn("rawPassword123");

        // 4. Mock static SecurityContextHolder (required for loginUser)
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {

            // Setup SecurityContext and Authentication mocks
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(userEmail);

            // Act
            UserInfo result = userService.loginUser();

            // Assert
            // 1. Verify finds the user
            verify(userRepo, times(1)).findByEmail(userEmail);

            // 2. Verify token generation
            verify(jwtUtil, times(1)).generateToken(userEmail);

            // 3. Verify save was called and updated token/lastLogin
            verify(userRepo, times(1)).save(any(UserInfo.class));

            // 4. Verify the returned object has the correct decrypted password and new token
            assertNotNull(result);
            assertEquals("rawPassword123", result.getPassword());
            assertEquals(newToken, result.getToken());
            // Since we mocked the save to return the same object, the decrypted password should be set on it
        }
    }
}