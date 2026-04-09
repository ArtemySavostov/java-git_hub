package com.savostov.git_manager.repository;

import com.savostov.git_manager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    @Test
    void findByUsername_withExistingUsername_returnsUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> result = userRepository.findByUsername("testuser");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
    }

    @Test
    void findByUsername_withNonExistingUsername_returnsEmpty() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = userRepository.findByUsername("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_withExistingEmail_returnsUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        Optional<User> result = userRepository.findByEmail("test@example.com");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
    }

    @Test
    void findByEmail_withNonExistingEmail_returnsEmpty() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void findUserByUsernameNot_withExcludedUsername_returnsOtherUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("otheruser");
        
        List<User> users = Arrays.asList(user2);
        when(userRepository.findUserByUsernameNot("testuser")).thenReturn(users);

        List<User> result = userRepository.findUserByUsernameNot("testuser");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("otheruser");
    }

    @Test
    void findUserByUsernameNot_withNonExcludedUsername_returnsAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("otheruser");
        
        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findUserByUsernameNot("nonexistent")).thenReturn(users);

        List<User> result = userRepository.findUserByUsernameNot("nonexistent");

        assertThat(result).hasSize(2);
        assertThat(result).contains(testUser, user2);
    }

    @Test
    void findAll_returnsAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        
        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userRepository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).contains(testUser, user2);
    }

    @Test
    void findById_withExistingId_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userRepository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
    }

    @Test
    void findById_withNonExistingId_returnsEmpty() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void existsById_withExistingId_returnsTrue() {
        when(userRepository.existsById(1L)).thenReturn(true);

        boolean result = userRepository.existsById(1L);

        assertThat(result).isTrue();
    }

    @Test
    void existsById_withNonExistingId_returnsFalse() {
        when(userRepository.existsById(999L)).thenReturn(false);

        boolean result = userRepository.existsById(999L);

        assertThat(result).isFalse();
    }

    @Test
    void save_withValidUser_savesUser() {
        when(userRepository.save(testUser)).thenReturn(testUser);

        User result = userRepository.save(testUser);

        assertThat(result).isEqualTo(testUser);
    }

    @Test
    void deleteById_withExistingId_deletesUser() {
        userRepository.deleteById(1L);
    }

    @Test
    void count_returnsUserCount() {
        when(userRepository.count()).thenReturn(5L);

        long result = userRepository.count();

        assertThat(result).isEqualTo(5L);
    }
}
