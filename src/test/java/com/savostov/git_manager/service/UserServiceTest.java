package com.savostov.git_manager.service;

import com.savostov.git_manager.model.User;
import com.savostov.git_manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RepositoryService repositoryService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("USER");
    }

    @Test
    void createUser_withValidData_createsUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.createUser("testuser", "test@example.com", "password123");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getRole()).isEqualTo("USER");
        verify(repositoryService).getUserRootDirectory(any(User.class));
    }

    @Test
    void createUser_withExistingUsername_throwsException() {
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.createUser("existinguser", "test@example.com", "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_withExistingEmail_throwsException() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.createUser("newuser", "existing@example.com", "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findUserById_withValidId_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findUserById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
        verify(userRepository).findById(1L);
    }

    @Test
    void findUserById_withInvalidId_returnsEmpty() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findUserById(999L);

        assertThat(result).isEmpty();
        verify(userRepository).findById(999L);
    }

    @Test
    void findUserByUsername_withValidUsername_returnsUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findUserByUsername("testuser");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void findUserByUsername_withInvalidUsername_returnsEmpty() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = userService.findUserByUsername("nonexistent");

        assertThat(result).isEmpty();
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void getAllUsers_returnsAllUsers() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertThat(result).isEqualTo(users);
        verify(userRepository).findAll();
    }

    @Test
    void getUserByUsername_withValidUsername_returnsUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserByUsername("testuser");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void subscriber_withValidIds_addsSubscription() {
        User subscriber = new User();
        subscriber.setId(1L);
        User targetUser = new User();
        targetUser.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(subscriber));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        userService.subscriber(1L, 2L);

        assertThat(subscriber.getSubscriptions()).contains(targetUser);
        verify(userRepository).save(subscriber);
    }

    @Test
    void subscriber_withInvalidSubscriberId_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.subscriber(999L, 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Subscriber not found");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void subscriber_withInvalidTargetId_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.subscriber(1L, 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User to subscribe not found");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void unsubscribe_withValidIds_removesSubscription() {
        User subscriber = new User();
        subscriber.setId(1L);
        User targetUser = new User();
        targetUser.setId(2L);
        subscriber.getSubscriptions().add(targetUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(subscriber));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        userService.unsubscribe(1L, 2L);

        assertThat(subscriber.getSubscriptions()).doesNotContain(targetUser);
        verify(userRepository).save(subscriber);
    }

    @Test
    void unsubscribe_withInvalidSubscriberId_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.unsubscribe(999L, 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Subscriber not found");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void unsubscribe_withInvalidTargetId_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.unsubscribe(1L, 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User to subscribe not found");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findUserByUsernameNot_returnsUsersExcludingSpecified() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findUserByUsernameNot("testuser")).thenReturn(users);

        List<User> result = userService.findUserByUsernameNot("testuser");

        assertThat(result).isEqualTo(users);
        verify(userRepository).findUserByUsernameNot("testuser");
    }

    @Test
    void getFollowingCount_withValidUser_returnsCount() {
        User user = new User();
        user.setId(1L);
        User follower = new User();
        user.getSubscribers().add(follower);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        int count = userService.getFollowingCount(1L);

        assertThat(count).isEqualTo(1);
        verify(userRepository).findById(1L);
    }

    @Test
    void getFollowingCount_withInvalidUser_returnsZero() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        int count = userService.getFollowingCount(999L);

        assertThat(count).isEqualTo(0);
        verify(userRepository).findById(999L);
    }

    @Test
    void getFollowersCount_withValidUser_returnsCount() {
        User user = new User();
        user.setId(1L);
        User following = new User();
        user.getSubscriptions().add(following);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        int count = userService.getFollowersCount(1L);

        assertThat(count).isEqualTo(1);
        verify(userRepository).findById(1L);
    }

    @Test
    void getFollowersCount_withInvalidUser_returnsZero() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        int count = userService.getFollowersCount(999L);

        assertThat(count).isEqualTo(0);
        verify(userRepository).findById(999L);
    }
}
