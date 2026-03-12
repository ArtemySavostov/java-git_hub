package com.savostov.git_manager.service;

import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.repository.MemberRepository;
import com.savostov.git_manager.repository.RepositoryRepository;
import com.savostov.git_manager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryServiceTest {

    @Mock
    private RepositoryRepository repositoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RepositoryService repositoryService;

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

