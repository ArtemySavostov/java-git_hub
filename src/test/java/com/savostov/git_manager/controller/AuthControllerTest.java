package com.savostov.git_manager.controller;

import com.savostov.git_manager.model.User;
import com.savostov.git_manager.service.RepositoryService;
import com.savostov.git_manager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Model model;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    }

    @Test
    void registerForm_returnsRegisterViewAndAddsUserToModel() {
        String view = authController.registerForm(model);

        assertThat(view).isEqualTo("register");
        verify(model).addAttribute("user", any(User.class));
    }

    @Test
    void registerUser_withValidData_createsUserAndRedirectsToLogin() {
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.empty());

        String view = authController.registerUser("testuser", "test@example.com", "password123", model);

        assertThat(view).isEqualTo("redirect:/login");
        verify(userService).createUser("testuser", "test@example.com", "password123");
    }

    @Test
    void registerUser_withEmptyUsername_returnsRegisterViewWithError() {
        String view = authController.registerUser("", "test@example.com", "password123", model);

        assertThat(view).isEqualTo("register");
        verify(model).addAttribute("errorMessage", "???????? ??? ???");
        verify(userService, never()).createUser(anyString(), anyString(), anyString());
    }

    @Test
    void registerUser_withEmptyEmail_returnsRegisterViewWithError() {
        String view = authController.registerUser("testuser", "", "password123", model);

        assertThat(view).isEqualTo("register");
        verify(model).addAttribute("errorMessage", "???????? ??? ???");
        verify(userService, never()).createUser(anyString(), anyString(), anyString());
    }

    @Test
    void registerUser_withEmptyPassword_returnsRegisterViewWithError() {
        String view = authController.registerUser("testuser", "test@example.com", "", model);

        assertThat(view).isEqualTo("register");
        verify(model).addAttribute("errorMessage", "???????? ??? ???");
        verify(userService, never()).createUser(anyString(), anyString(), anyString());
    }

    @Test
    void registerUser_withNullUsername_returnsRegisterViewWithError() {
        String view = authController.registerUser(null, "test@example.com", "password123", model);

        assertThat(view).isEqualTo("register");
        verify(model).addAttribute("errorMessage", "???????? ??? ???");
        verify(userService, never()).createUser(anyString(), anyString(), anyString());
    }

    @Test
    void registerUser_withExistingUsername_returnsRegisterViewWithError() {
        when(userService.getUserByUsername("existinguser")).thenReturn(Optional.of(new User()));

        String view = authController.registerUser("existinguser", "test@example.com", "password123", model);

        assertThat(view).isEqualTo("register");
        verify(model).addAttribute("errorMessage", "User already exists");
        verify(userService, never()).createUser(anyString(), anyString(), anyString());
    }

    @Test
    void loginForm_returnsLoginView() {
        String view = authController.loginForm();

        assertThat(view).isEqualTo("login");
    }
}
