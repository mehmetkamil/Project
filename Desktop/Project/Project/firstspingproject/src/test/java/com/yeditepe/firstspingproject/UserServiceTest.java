package com.yeditepe.firstspingproject;

import com.yeditepe.firstspingproject.dto.UserDTO;
import com.yeditepe.firstspingproject.entity.User;
import com.yeditepe.firstspingproject.repository.UserRepository;
import com.yeditepe.firstspingproject.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 * Tests user registration, validation, and retrieval operations
 */
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
    }

    @Test
    void testGetAllUsers_ReturnsUserList() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<UserDTO> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserById_ExistingUser_ReturnsUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<UserDTO> result = userService.getUserById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserById_NonExistingUser_ReturnsEmpty() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<UserDTO> result = userService.getUserById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testGetUserByUsername_ExistingUser_ReturnsUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        Optional<UserDTO> result = userService.getUserByUsername("testuser");

        // Then
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testValidateLogin_ValidCredentials_ReturnsTrue() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // When
        boolean result = userService.validateLogin("testuser", "password123");

        // Then
        assertTrue(result);
    }

    @Test
    void testValidateLogin_InvalidPassword_ReturnsFalse() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // When
        boolean result = userService.validateLogin("testuser", "wrongpassword");

        // Then
        assertFalse(result);
    }

    @Test
    void testValidateLogin_NonExistingUser_ReturnsFalse() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When
        boolean result = userService.validateLogin("nonexistent", "password");

        // Then
        assertFalse(result);
    }

    @Test
    void testDeleteUser_ExistingUser_Success() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> userService.deleteUser(1L));

        // Then
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUser_NonExistingUser_ThrowsException() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.deleteUser(999L));
    }
}
