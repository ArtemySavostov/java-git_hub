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
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

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
    private HomeController homeController;

    private User testUser;
    private Repo testRepo;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testRepo = new Repo();
        testRepo.setId(1L);
        testRepo.setName("testrepo");
        testRepo.setOwner(testUser);
    }

    @Test
    void homePage_withValidUser_returnsHome() {
        when(authentication.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(repositoryService.getRepositoryByOwnerId("testuser")).thenReturn(Arrays.asList(testRepo));
        when(userRepository.countFollowers(1L)).thenReturn(5);
        when(userRepository.countFollowing(1L)).thenReturn(3);

        String view = homeController.homePage(model);

        assertThat(view).isEqualTo("home");
        verify(model).addAttribute("followersCount", 5);
        verify(model).addAttribute("followingCount", 3);
        verify(model).addAttribute("repositories", Arrays.asList(testRepo));
        verify(model).addAttribute("user", testUser);
    }

    @Test
    void homePage_withUserNotFound_throwsException() {
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeController.homePage(model))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void homePage_withNoRepositories_returnsHome() {
        when(authentication.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(repositoryService.getRepositoryByOwnerId("testuser")).thenReturn(Arrays.asList());
        when(userRepository.countFollowers(1L)).thenReturn(0);
        when(userRepository.countFollowing(1L)).thenReturn(0);

        String view = homeController.homePage(model);

        assertThat(view).isEqualTo("home");
        verify(model).addAttribute("repositories", Arrays.asList());
    }

    @Test
    void newRepositoryForm_returnsForm() {
        String view = homeController.newRepositoryForm(model);

        assertThat(view).isEqualTo("repositories_form");
        verify(model).addAttribute("repo", new Repo());
    }

    @Test
    void createRepository_withValidUser_createsAndRedirects() {
        when(authentication.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(repositoryService.createRepository(testUser, "testrepo", false)).thenReturn(testRepo);

        String view = homeController.createRepository(testRepo);

        assertThat(view).isEqualTo("redirect:/home");
        verify(repositoryService).createRepository(testUser, "testrepo", false);
    }

    @Test
    void createRepository_withUserNotFound_throwsException() {
        when(authentication.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeController.createRepository(testRepo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void editRepositoryForm_withValidOwner_returnsEditForm() {
        when(authentication.getName()).thenReturn("testuser");
        when(repositoryService.getRepositoryById(1L)).thenReturn(testRepo);

        String view = homeController.editRepositoryForm(1L, model);

        assertThat(view).isEqualTo("repository_edit");
        verify(model).addAttribute("repo", testRepo);
    }

    @Test
    void editRepositoryForm_withInvalidOwner_redirectsToHome() {
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        testRepo.setOwner(otherUser);

        when(authentication.getName()).thenReturn("testuser");
        when(repositoryService.getRepositoryById(1L)).thenReturn(testRepo);

        String view = homeController.editRepositoryForm(1L, model);

        assertThat(view).isEqualTo("redirect:/home");
    }

    @Test
    void editRepositoryForm_withNullRepo_redirectsToHome() {
        when(authentication.getName()).thenReturn("testuser");
        when(repositoryService.getRepositoryById(1L)).thenReturn(null);

        String view = homeController.editRepositoryForm(1L, model);

        assertThat(view).isEqualTo("redirect:/home");
    }

    @Test
    void updateRepository_withValidOwner_updatesAndRedirects() {
        when(authentication.getName()).thenReturn("testuser");
        when(repositoryService.getRepositoryById(1L)).thenReturn(testRepo);

        String view = homeController.updateRepository(1L, testRepo);

        assertThat(view).isEqualTo("redirect:/home");
        verify(repositoryService).updateRepository(1L, "testrepo", null, false);
    }

    @Test
    void updateRepository_withInvalidOwner_redirectsToHome() {
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        testRepo.setOwner(otherUser);

        when(authentication.getName()).thenReturn("testuser");
        when(repositoryService.getRepositoryById(1L)).thenReturn(testRepo);

        String view = homeController.updateRepository(1L, testRepo);

        assertThat(view).isEqualTo("redirect:/home");
        verify(repositoryService, never()).updateRepository(anyLong(), anyString(), anyString(), anyBoolean());
    }

    @Test
    void deleteRepository_withValidOwner_deletesAndRedirects() {
        when(authentication.getName()).thenReturn("testuser");
        when(repositoryService.getRepositoryById(1L)).thenReturn(testRepo);

        String view = homeController.deleteRepository(1L);

        assertThat(view).isEqualTo("redirect:/home");
        verify(repositoryService).deleteRepository(1L);
    }

    @Test
    void deleteRepository_withInvalidOwner_redirectsToHome() {
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        testRepo.setOwner(otherUser);

        when(authentication.getName()).thenReturn("testuser");
        when(repositoryService.getRepositoryById(1L)).thenReturn(testRepo);

        String view = homeController.deleteRepository(1L);

        assertThat(view).isEqualTo("redirect:/home");
        verify(repositoryService, never()).deleteRepository(anyLong());
    }

    @Test
    void deleteRepository_withNullRepo_redirectsToHome() {
        when(authentication.getName()).thenReturn("testuser");
        when(repositoryService.getRepositoryById(1L)).thenReturn(null);

        String view = homeController.deleteRepository(1L);

        assertThat(view).isEqualTo("redirect:/home");
        verify(repositoryService, never()).deleteRepository(anyLong());
    }
}
