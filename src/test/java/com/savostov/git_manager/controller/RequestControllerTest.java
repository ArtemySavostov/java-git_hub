package com.savostov.git_manager.controller;

import com.savostov.git_manager.model.MatchRequestCreateRepo;
import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.model.User;
import com.savostov.git_manager.repository.UserRepository;
import com.savostov.git_manager.repository.RepositoryRepository;
import com.savostov.git_manager.service.MemberService;
import com.savostov.git_manager.service.RepositoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RepositoryRepository repositoryRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private RepositoryService repositoryService;

    @InjectMocks
    private RequestController requestController;

    private User testUser;
    private User collaboratorUser;
    private Repo testRepo;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("owner");

        collaboratorUser = new User();
        collaboratorUser.setId(2L);
        collaboratorUser.setUsername("collaborator");

        testRepo = new Repo();
        testRepo.setId(1L);
        testRepo.setName("test-repo");
        testRepo.setOwner(testUser);
    }

    @Test
    void createRepo_withValidData_createsRepository() {
        MatchRequestCreateRepo request = new MatchRequestCreateRepo();
        request.setRepoName("test-repo");
        request.setOwner("owner");
        request.setUserCollaborator("collaborator");

        when(userRepository.getByUsername("owner")).thenReturn(testUser);
        when(userRepository.getByUsername("collaborator")).thenReturn(collaboratorUser);
        when(repositoryService.createRepository(testUser, "test-repo", false)).thenReturn(testRepo);

        ResponseEntity<String> response = requestController.createRepo(request, "secret");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Repository created successfully");
        verify(repositoryService).createRepository(testUser, "test-repo", false);
        verify(memberService).addMemberToRepo(testRepo.getId(), collaboratorUser.getId(), "READ");
    }

    @Test
    void createRepo_withMissingApiKey_returnsBadRequest() {
        MatchRequestCreateRepo request = new MatchRequestCreateRepo();

        ResponseEntity<String> response = requestController.createRepo(request, null);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("API Key header is missing");
    }

    @Test
    void createRepo_withInvalidApiKey_returnsUnauthorized() {
        MatchRequestCreateRepo request = new MatchRequestCreateRepo();

        ResponseEntity<String> response = requestController.createRepo(request, "invalid");

        assertThat(response.getStatusCodeValue()).isEqualTo(401);
        assertThat(response.getBody()).isEqualTo("Invalid API Key");
    }

    @Test
    void createRepo_withNonExistentOwner_returnsBadRequest() {
        MatchRequestCreateRepo request = new MatchRequestCreateRepo();
        request.setRepoName("test-repo");
        request.setOwner("nonexistent");
        request.setUserCollaborator("collaborator");

        when(userRepository.getByUsername("nonexistent")).thenReturn(null);

        ResponseEntity<String> response = requestController.createRepo(request, "secret");

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Owner or Collaborator email not found");
    }

    @Test
    void createRepo_withNonExistentCollaborator_returnsBadRequest() {
        MatchRequestCreateRepo request = new MatchRequestCreateRepo();
        request.setRepoName("test-repo");
        request.setOwner("owner");
        request.setUserCollaborator("nonexistent");

        when(userRepository.getByUsername("owner")).thenReturn(testUser);
        when(userRepository.getByUsername("nonexistent")).thenReturn(null);

        ResponseEntity<String> response = requestController.createRepo(request, "secret");

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Owner or Collaborator email not found");
    }

    @Test
    void createRepo_withException_returnsInternalServerError() {
        MatchRequestCreateRepo request = new MatchRequestCreateRepo();
        request.setRepoName("test-repo");
        request.setOwner("owner");
        request.setUserCollaborator("collaborator");

        when(userRepository.getByUsername("owner")).thenReturn(testUser);
        when(userRepository.getByUsername("collaborator")).thenReturn(collaboratorUser);
        when(repositoryService.createRepository(testUser, "test-repo", false))
                .thenThrow(new RuntimeException("Creation failed"));

        ResponseEntity<String> response = requestController.createRepo(request, "secret");

        assertThat(response.getStatusCodeValue()).isEqualTo(500);
        assertThat(response.getBody()).contains("An error occurred: Creation failed");
    }
}
