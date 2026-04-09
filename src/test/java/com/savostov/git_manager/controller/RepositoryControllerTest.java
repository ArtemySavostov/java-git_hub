package com.savostov.git_manager.controller;

import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.model.User;
import com.savostov.git_manager.repository.RepositoryRepository;
import com.savostov.git_manager.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryControllerTest {

    @Mock
    private GitService gitService;

    @Mock
    private RepositoryRepository repositoryRepository;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private FileStructureService fileStructureService;

    @Mock
    private UserService userService;

    @Mock
    private MemberService memberService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Model model;

    @InjectMocks
    private RepositoryController repositoryController;

    private Repo testRepo;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testRepo = new Repo();
        testRepo.setId(1L);
        testRepo.setName("test-repo");
        testRepo.setPath("/path/to/repo");
        testRepo.setOwner(testUser);
    }

    @Test
    void showRepository_withValidId_returnsRepositoryView() throws ChangeSetPersister.NotFoundException {
        when(repositoryRepository.findById(1L)).thenReturn(Optional.of(testRepo));

        String view = repositoryController.showRepository(1L, "", model);

        assertThat(view).isEqualTo("repository");
        verify(model).addAttribute("repo", testRepo);
        verify(model).addAttribute("currentPath", "");
    }

    @Test
    void showRepository_withInvalidId_throwsNotFoundException() {
        when(repositoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repositoryController.showRepository(999L, "", model))
                .isInstanceOf(ChangeSetPersister.NotFoundException.class);
    }

    @Test
    void showListRepositories_returnsRepositoriesListView() {
        List<Repo> repos = Arrays.asList(testRepo);
        when(repositoryRepository.findAll()).thenReturn(repos);

        String view = repositoryController.showListRepositories(model);

        assertThat(view).isEqualTo("repositories_list");
        verify(model).addAttribute("repo", repos);
    }

    @Test
    void showFiles_withValidRequest_returnsFileView() throws Exception {
        when(repositoryRepository.findById(1L)).thenReturn(Optional.of(testRepo));
        when(request.getRequestURI()).thenReturn("/repository/1/file/src/test.txt");
        when(request.getContextPath()).thenReturn("");
        when(gitService.getFileContent("/path/to/repo", "src/test.txt")).thenReturn("file content");

        String view = repositoryController.showFiles(1L, request, model);

        assertThat(view).isEqualTo("file");
        verify(model).addAttribute("repo", testRepo);
        verify(model).addAttribute("filePath", "src/test.txt");
        verify(model).addAttribute("content", "file content");
    }

    @Test
    void showFiles_withInvalidRepoId_throwsNotFoundException() {
        when(request.getRequestURI()).thenReturn("/repository/999/file/test.txt");
        when(request.getContextPath()).thenReturn("");
        when(repositoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repositoryController.showFiles(999L, request, model))
                .isInstanceOf(ChangeSetPersister.NotFoundException.class);
    }

    @Test
    void loadFiles_withValidFiles_uploadsAndCommits() throws Exception {
        MultipartFile[] files = new MultipartFile[0];
        when(repositoryRepository.getReferenceById(1L)).thenReturn(testRepo);

        String view = repositoryController.loadFiles(1L, files, model);

        assertThat(view).isEqualTo("redirect:/repository/1");
        verify(repositoryService).uplodFiles(1L, files);
        verify(gitService).addFiles("/path/to/repo");
        verify(gitService).commitFiles("/path/to/repo", "Added new files");
    }

    @Test
    void getFileContent_withValidRequest_returnsContent() throws Exception {
        when(repositoryService.getFileContentFromGit(1L, "test.txt")).thenReturn("test content");

        ResponseEntity<String> response = repositoryController.getFileContent(1L, "test.txt");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("test content");
    }

    @Test
    void getFileContent_withInvalidPath_returnsNotFound() throws Exception {
        when(repositoryService.getFileContentFromGit(1L, "nonexistent.txt"))
                .thenThrow(new IOException("File not found"));

        ResponseEntity<String> response = repositoryController.getFileContent(1L, "nonexistent.txt");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("File not found or error reading content");
    }

    @Test
    void collaborations_withValidRepo_returnsCollaborationsView() {
        List<User> users = Arrays.asList(testUser);
        when(repositoryRepository.getReferenceById(1L)).thenReturn(testRepo);
        when(userService.findUserByUsernameNot("testuser")).thenReturn(users);

        String view = repositoryController.collaborations(1L, model);

        assertThat(view).isEqualTo("collaborations");
        verify(model).addAttribute(users);
    }

    @Test
    void addMemberToRepository_withValidData_addsMemberAndRedirects() {
        String view = repositoryController.addMemberToRepository(1L, 2L, "developer");

        assertThat(view).isEqualTo("redirect:/repository/1");
        verify(memberService).addMemberToRepo(1L, 2L, "developer");
    }

    @Test
    void getCollaborationsList_withValidUser_returnsCollaborationsListView() {
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        
        org.springframework.security.core.context.SecurityContext context = mock(org.springframework.security.core.context.SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);

        List<Repo> collaborationList = Arrays.asList(testRepo);
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(repositoryService.getListColloborations(1L)).thenReturn(collaborationList);

        String view = repositoryController.getCollaborationsList(model);

        assertThat(view).isEqualTo("collaborations_list");
        verify(model).addAttribute(eq("repos"), eq(collaborationList));
        verify(model).addAttribute(eq("repoOwnerMap"), any(Map.class));
    }

    @Test
    void getCollaborationsList_withInvalidUser_redirectsToLogin() {
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getName()).thenReturn("nonexistent");
        
        org.springframework.security.core.context.SecurityContext context = mock(org.springframework.security.core.context.SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);

        when(userService.getUserByUsername("nonexistent")).thenReturn(Optional.empty());

        String view = repositoryController.getCollaborationsList(model);

        assertThat(view).isEqualTo("redirect:/login");
    }
}
