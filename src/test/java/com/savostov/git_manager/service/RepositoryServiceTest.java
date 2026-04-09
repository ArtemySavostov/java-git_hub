package com.savostov.git_manager.service;

import com.savostov.git_manager.model.Member;
import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.model.User;
import com.savostov.git_manager.repository.MemberRepository;
import com.savostov.git_manager.repository.RepositoryRepository;
import com.savostov.git_manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryServiceTest {

    @Mock
    private RepositoryRepository repositoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GitService gitService;

    @InjectMocks
    private RepositoryService repositoryService;

    private User testUser;
    private Repo testRepo;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testRepo = new Repo();
        testRepo.setId(1L);
        testRepo.setName("test-repo");
        testRepo.setPath("/repos/testuser/test-repo");
    }

    @Test
    void createRepository_createsRepoAndMember() {
        when(repositoryRepository.save(any(Repo.class))).thenReturn(testRepo);
        when(memberRepository.save(any(Member.class))).thenReturn(new Member());

        Repo result = repositoryService.createRepository(testUser, "test-repo", false);

        assertThat(result).isEqualTo(testRepo);
        verify(repositoryRepository).save(any(Repo.class));
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void createDefaultRepository_createsRepo() {
        when(repositoryRepository.save(any(Repo.class))).thenReturn(testRepo);

        Repo result = repositoryService.createDefaultRepository(testUser);

        assertThat(result).isEqualTo(testRepo);
        verify(repositoryRepository).save(any(Repo.class));
    }

    @Test
    void getRepositoryByOwnerId_withValidUser_returnsRepositories() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(repositoryRepository.findByOwnerId(1L)).thenReturn(List.of(testRepo));

        List<Repo> result = repositoryService.getRepositoryByOwnerId("testuser");

        assertThat(result).containsExactly(testRepo);
    }

    @Test
    void getRepositoryByOwnerId_withInvalidUser_returnsEmptyList() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        List<Repo> result = repositoryService.getRepositoryByOwnerId("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void getRepositoryWithOutPrivate_filtersPrivateRepos() {
        Repo privateRepo = new Repo();
        privateRepo.setPrivate(true);
        Repo publicRepo = new Repo();
        publicRepo.setPrivate(false);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(repositoryRepository.findByOwnerId(1L)).thenReturn(List.of(privateRepo, publicRepo));

        List<Repo> result = repositoryService.getRepositoryWithOutPrivate("testuser");

        assertThat(result).containsExactly(publicRepo);
    }

    @Test
    void getRepositoryByName_returnsRepository() {
        when(repositoryRepository.getRepositoryByName("test-repo")).thenReturn(Optional.of(testRepo));

        Optional<Repo> result = repositoryService.getRepositoryByName("test-repo");

        assertThat(result).contains(testRepo);
    }

    @Test
    void getRepositoryByName_returnsEmpty() {
        when(repositoryRepository.getRepositoryByName("nonexistent")).thenReturn(Optional.empty());

        Optional<Repo> result = repositoryService.getRepositoryByName("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void getRepositoryById_returnsRepository() {
        when(repositoryRepository.getById(1L)).thenReturn(testRepo);

        Repo result = repositoryService.getRepositoryById(1L);

        assertThat(result).isEqualTo(testRepo);
    }

    @Test
    void getListColloborations_returnsList() {
        when(repositoryRepository.findReposWhereUserIsCollaboratorNotOwner(1L)).thenReturn(List.of(testRepo));

        List<Repo> result = repositoryService.getListColloborations(1L);

        assertThat(result).containsExactly(testRepo);
    }

    @Test
    void getFileStructure_returnsStructure() throws Exception {
        when(repositoryRepository.getReferenceById(1L)).thenReturn(testRepo);
        when(gitService.getFiles(anyString())).thenReturn(List.of("file1.txt", "dir1"));
        when(Files.isDirectory(any(Path.class))).thenReturn(false, true);

        List<Map<String, Object>> result = repositoryService.getFileStructure(1L, "");

        assertThat(result).hasSize(2);
        verify(gitService).getFiles(anyString());
    }

    @Test
    void getFileContentFromGit_returnsContent() throws Exception {
        when(repositoryRepository.getReferenceById(1L)).thenReturn(testRepo);
        when(gitService.getFileContent(anyString(), anyString())).thenReturn("file content");

        String result = repositoryService.getFileContentFromGit(1L, "test.txt");

        assertThat(result).isEqualTo("file content");
        verify(gitService).getFileContent(anyString(), anyString());
    }

    @Test
    void uplodFiles_uploadsFiles() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.txt");
        when(mockFile.getInputStream()).thenReturn(Files.newInputStream(Paths.get("test.txt")));

        when(repositoryRepository.getReferenceById(1L)).thenReturn(testRepo);
        when(Files.exists(any(Path.class))).thenReturn(true);
        when(Files.createDirectories(any(Path.class))).thenReturn(null);

        repositoryService.uplodFiles(1L, new MultipartFile[]{mockFile});

        verify(repositoryRepository).getReferenceById(1L);
    }

    @Test
    void updateRepository_updatesFieldsAndSaves() {
        Repo existing = new Repo();
        existing.setId(10L);
        existing.setName("old");
        existing.setDescription("old desc");
        existing.setPrivate(false);

        when(repositoryRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(repositoryRepository.save(any(Repo.class))).thenAnswer(inv -> inv.getArgument(0));

        Repo updated = repositoryService.updateRepository(10L, "new", "new desc", true);

        assertThat(updated.getId()).isEqualTo(10L);
        assertThat(updated.getName()).isEqualTo("new");
        assertThat(updated.getDescription()).isEqualTo("new desc");
        assertThat(updated.isPrivate()).isTrue();

        ArgumentCaptor<Repo> captor = ArgumentCaptor.forClass(Repo.class);
        verify(repositoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("new");
    }

    @Test
    void updateRepository_whenNotFound_throws() {
        when(repositoryRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repositoryService.updateRepository(404L, "x", "y", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Repository not found");
    }

    @Test
    void deleteRepository_whenExists_deletes() {
        when(repositoryRepository.existsById(1L)).thenReturn(true);

        repositoryService.deleteRepository(1L);

        verify(repositoryRepository).deleteById(1L);
    }

    @Test
    void deleteRepository_whenNotExists_noDeleteCall() {
        when(repositoryRepository.existsById(2L)).thenReturn(false);

        repositoryService.deleteRepository(2L);

        verify(repositoryRepository, never()).deleteById(anyLong());
    }
}

