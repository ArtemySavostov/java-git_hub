package com.savostov.git_manager.controller;

import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.model.User;
import com.savostov.git_manager.repository.UserRepository;
import com.savostov.git_manager.service.RepositoryService;
import com.savostov.git_manager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private User currentUser;
    private Repo testRepo;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        currentUser = new User();
        currentUser.setId(2L);
        currentUser.setUsername("currentuser");
        currentUser.setEmail("current@example.com");

        testRepo = new Repo();
        testRepo.setId(1L);
        testRepo.setName("testrepo");
    }

    @Test
    void getUserById_withValidId_returnsUserPage() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(repositoryService.getRepositoryWithOutPrivate("testuser")).thenReturn(Arrays.asList());
        when(userRepository.countFollowers(1L)).thenReturn(5);
        when(userRepository.countFollowing(1L)).thenReturn(3);

        String view = userController.getUserById(1L, model);

        assertThat(view).isEqualTo("user_page");
        verify(model).addAttribute("followersCount", 5);
        verify(model).addAttribute("followingCount", 3);
        verify(model).addAttribute("repositories", Arrays.asList());
        verify(model).addAttribute("user", testUser);
    }

    @Test
    void getUserById_withInvalidId_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.getUserById(999L, model))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getUserById_withRepositories_returnsUserPageWithRepos() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(repositoryService.getRepositoryWithOutPrivate("testuser")).thenReturn(Arrays.asList(testRepo));
        when(userRepository.countFollowers(1L)).thenReturn(10);
        when(userRepository.countFollowing(1L)).thenReturn(7);

        String view = userController.getUserById(1L, model);

        assertThat(view).isEqualTo("user_page");
        verify(model).addAttribute("followersCount", 10);
        verify(model).addAttribute("followingCount", 7);
        verify(model).addAttribute("repositories", Arrays.asList(testRepo));
        verify(model).addAttribute("user", testUser);
    }

    @Test
    void listUsers_returnsUsersList() {
        when(authentication.getName()).thenReturn("currentuser");
        when(userService.findUserByUsernameNot("currentuser")).thenReturn(Arrays.asList(testUser));
        when(userService.getUserByUsername("currentuser")).thenReturn(Optional.of(currentUser));
        when(userRepository.getFollowingList(2L)).thenReturn(Arrays.asList(1L));

        String view = userController.listUsers(model);

        assertThat(view).isEqualTo("user_list");
        verify(model).addAttribute("followingUsers", Arrays.asList(1L));
        verify(model).addAttribute("users", Arrays.asList(testUser));
        verify(model).addAttribute("currentUserId", 2L);
    }

    @Test
    void listUsers_withNoCurrentUser_returnsUsersListWithNullId() {
        when(authentication.getName()).thenReturn("currentuser");
        when(userService.findUserByUsernameNot("currentuser")).thenReturn(Arrays.asList(testUser));
        when(userService.getUserByUsername("currentuser")).thenReturn(Optional.empty());

        String view = userController.listUsers(model);

        assertThat(view).isEqualTo("user_list");
        verify(model).addAttribute("currentUserId", null);
    }

    @Test
    void listUsers_withEmptyFollowingList_returnsUsersList() {
        when(authentication.getName()).thenReturn("currentuser");
        when(userService.findUserByUsernameNot("currentuser")).thenReturn(Arrays.asList(testUser));
        when(userService.getUserByUsername("currentuser")).thenReturn(Optional.of(currentUser));
        when(userRepository.getFollowingList(2L)).thenReturn(Arrays.asList());

        String view = userController.listUsers(model);

        assertThat(view).isEqualTo("user_list");
        verify(model).addAttribute("followingUsers", Arrays.asList());
    }

    @Test
    void listUsers_withNullCurrentUser_handlesGracefully() {
        when(authentication.getName()).thenReturn("nonexistent");
        when(userService.findUserByUsernameNot("nonexistent")).thenReturn(Arrays.asList());
        when(userService.getUserByUsername("nonexistent")).thenReturn(Optional.empty());

        String view = userController.listUsers(model);

        assertThat(view).isEqualTo("user_list");
        verify(model).addAttribute("followingUsers", List.of());
        verify(model).addAttribute("currentUserId", null);
        verify(userRepository, never()).getFollowingList(any());
    }

    @Test
    void subscribe_withValidIds_returnsSuccess() {
        ResponseEntity<String> response = userController.subscribe(1L, 2L, model);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Subscribed successfully");
        verify(userService).subscriber(1L, 2L);
    }

    @Test
    void subscribe_withSameIds_returnsSuccess() {
        ResponseEntity<String> response = userController.subscribe(1L, 1L, model);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Subscribed successfully");
        verify(userService).subscriber(1L, 1L);
    }

    @Test
    void unsubscribe_withValidIds_returnsSuccess() {
        ResponseEntity<String> response = userController.unsubscribe(1L, 2L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Unsubscribed successfully");
        verify(userService).unsubscribe(1L, 2L);
    }

    @Test
    void unsubscribe_withSameIds_returnsSuccess() {
        ResponseEntity<String> response = userController.unsubscribe(1L, 1L);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Unsubscribed successfully");
        verify(userService).unsubscribe(1L, 1L);
    }
}
