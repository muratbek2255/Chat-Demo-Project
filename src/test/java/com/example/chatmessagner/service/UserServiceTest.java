package com.example.chatmessagner.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.chatmessagner.entity.User;
import com.example.chatmessagner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setPhoneNumber("1234567890");
        testUser.setPassword("password");
    }

    @Test
    void createUser_shouldCreateUser() {
        when(bCryptPasswordEncoder.encode(testUser.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.createUser(testUser);

        verify(userRepository).save(any(User.class));
        assertEquals("encodedPassword", testUser.getPassword());
    }

    @Test
    void updateUser_shouldUpdateUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateUser(testUser);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        userService.deleteUser(testUser.getPhoneNumber());

        verify(userRepository).deleteByPhoneNumber(testUser.getPhoneNumber());
    }

    @Test
    void changePassword_shouldChangeUserPassword() {
        when(bCryptPasswordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.changePassword("oldPassword", "newPassword");

        verify(userRepository).save(any(User.class));
        assertEquals("encodedNewPassword", testUser.getPassword());
    }

    @Test
    void userExists_shouldReturnTrueIfUserExists() {
        when(userRepository.findByPhoneNumber(testUser.getPhoneNumber())).thenReturn(Optional.of(testUser));

        assertTrue(userService.userExists(testUser.getPhoneNumber()));
    }

    @Test
    void userExists_shouldReturnFalseIfUserDoesNotExist() {
        when(userRepository.findByPhoneNumber(testUser.getPhoneNumber())).thenReturn(Optional.empty());

        assertFalse(userService.userExists(testUser.getPhoneNumber()));
    }

    @Test
    void loadUserByUsername_shouldLoadUserByUsername() {
        when(userRepository.findByPhoneNumber(testUser.getPhoneNumber())).thenReturn(Optional.of(testUser));

        UserDetails loadedUser = userService.loadUserByUsername(testUser.getPhoneNumber());

        assertNotNull(loadedUser);
        assertEquals(testUser.getPhoneNumber(), loadedUser.getUsername());
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundExceptionIfUserDoesNotExist() {
        when(userRepository.findByPhoneNumber(testUser.getPhoneNumber())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(testUser.getPhoneNumber()));
    }

    @Test
    void getById_shouldReturnUserById() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        Optional<User> loadedUser = userService.getById(testUser.getId());

        assertTrue(loadedUser.isPresent());
    }
}