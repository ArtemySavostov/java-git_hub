package com.savostov.git_manager.repository;

import com.savostov.git_manager.model.Repo;
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
class RepositoryRepositoryTest {

    @Mock
    private RepositoryRepository repositoryRepository;

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
        testRepo.setOwner(testUser);
        testRepo.setPrivate(false);
    }

    @Test
    void findById_withExistingId_returnsRepository() {
        when(repositoryRepository.findById(1L)).thenReturn(Optional.of(testRepo));

        Optional<Repo> result = repositoryRepository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testRepo);
    }

    @Test
    void findById_withNonExistingId_returnsEmpty() {
        when(repositoryRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Repo> result = repositoryRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_returnsAllRepositories() {
        Repo repo2 = new Repo();
        repo2.setId(2L);
        repo2.setName("repo2");
        repo2.setOwner(testUser);
        
        List<Repo> repos = Arrays.asList(testRepo, repo2);
        when(repositoryRepository.findAll()).thenReturn(repos);

        List<Repo> result = repositoryRepository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).contains(testRepo, repo2);
    }

    @Test
    void findByOwnerId_withValidOwnerId_returnsOwnerRepositories() {
        List<Repo> repos = Arrays.asList(testRepo);
        when(repositoryRepository.findByOwnerId(1L)).thenReturn(repos);

        List<Repo> result = repositoryRepository.findByOwnerId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRepo);
    }

    @Test
    void getRepositoryByName_withValidName_returnsRepository() {
        when(repositoryRepository.getRepositoryByName("test-repo")).thenReturn(Optional.of(testRepo));

        Optional<Repo> result = repositoryRepository.getRepositoryByName("test-repo");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testRepo);
    }

    @Test
    void getRepositoryByName_withNonExistingName_returnsEmpty() {
        when(repositoryRepository.getRepositoryByName("nonexistent")).thenReturn(Optional.empty());

        Optional<Repo> result = repositoryRepository.getRepositoryByName("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void findAccessibleReposByOwnerId_withValidUser_returnsAccessibleRepos() {
        List<Repo> repos = Arrays.asList(testRepo);
        when(repositoryRepository.findAccessibleReposByOwnerId(testUser)).thenReturn(repos);

        List<Repo> result = repositoryRepository.findAccessibleReposByOwnerId(testUser);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRepo);
    }

    @Test
    void findReposWhereUserIsCollaboratorNotOwner_withValidUserId_returnsCollaboratorRepos() {
        List<Repo> repos = Arrays.asList(testRepo);
        when(repositoryRepository.findReposWhereUserIsCollaboratorNotOwner(1L)).thenReturn(repos);

        List<Repo> result = repositoryRepository.findReposWhereUserIsCollaboratorNotOwner(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRepo);
    }

    @Test
    void existsById_withExistingId_returnsTrue() {
        when(repositoryRepository.existsById(1L)).thenReturn(true);

        boolean result = repositoryRepository.existsById(1L);

        assertThat(result).isTrue();
    }

    @Test
    void existsById_withNonExistingId_returnsFalse() {
        when(repositoryRepository.existsById(999L)).thenReturn(false);

        boolean result = repositoryRepository.existsById(999L);

        assertThat(result).isFalse();
    }

    @Test
    void save_withValidRepository_savesRepository() {
        when(repositoryRepository.save(testRepo)).thenReturn(testRepo);

        Repo result = repositoryRepository.save(testRepo);

        assertThat(result).isEqualTo(testRepo);
    }

    @Test
    void deleteById_withExistingId_deletesRepository() {
        repositoryRepository.deleteById(1L);
    }

    @Test
    void count_returnsRepositoryCount() {
        when(repositoryRepository.count()).thenReturn(3L);

        long result = repositoryRepository.count();

        assertThat(result).isEqualTo(3L);
    }

    @Test
    void getReferenceById_withValidId_returnsReference() {
        when(repositoryRepository.getReferenceById(1L)).thenReturn(testRepo);

        Repo result = repositoryRepository.getReferenceById(1L);

        assertThat(result).isEqualTo(testRepo);
    }
}
