package com.savostov.git_manager.service;

import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.repository.RepositoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStructureServiceTest {

    @Mock
    private GitService gitService;

    @Mock
    private RepositoryRepository repositoryRepository;

    @InjectMocks
    private FileStructureService fileStructureService;

    private Repo testRepo;

    @BeforeEach
    void setUp() {
        testRepo = new Repo();
        testRepo.setId(1L);
        testRepo.setName("test-repo");
        testRepo.setPath("/path/to/repo");
    }

    @Test
    void getFileStructure_withValidRepoAndEmptyPath_returnsFileStructure() throws Exception {
        GitService.Node rootNode = new GitService.Node("root", "", true);
        GitService.Node childNode = new GitService.Node("file.txt", "file.txt", false);
        rootNode.addChild(childNode);

        when(repositoryRepository.getReferenceById(1L)).thenReturn(testRepo);
        when(gitService.getFileStructure("/path/to/repo")).thenReturn(rootNode);

        List<Map<String, Object>> result = fileStructureService.getFileStructure(1L, "");

        assertThat(result).isNotEmpty();
        verify(repositoryRepository).getReferenceById(1L);
        verify(gitService).getFileStructure("/path/to/repo");
    }

    @Test
    void getFileStructure_withValidRepoAndNullPath_returnsFileStructure() throws Exception {
        GitService.Node rootNode = new GitService.Node("root", "", true);
        GitService.Node childNode = new GitService.Node("file.txt", "file.txt", false);
        rootNode.addChild(childNode);

        when(repositoryRepository.getReferenceById(1L)).thenReturn(testRepo);
        when(gitService.getFileStructure("/path/to/repo")).thenReturn(rootNode);

        List<Map<String, Object>> result = fileStructureService.getFileStructure(1L, null);

        assertThat(result).isNotEmpty();
        verify(repositoryRepository).getReferenceById(1L);
        verify(gitService).getFileStructure("/path/to/repo");
    }

    @Test
    void getFileStructure_withValidRepoAndRootPath_returnsFileStructure() throws Exception {
        GitService.Node rootNode = new GitService.Node("root", "", true);
        GitService.Node childNode = new GitService.Node("file.txt", "file.txt", false);
        rootNode.addChild(childNode);

        when(repositoryRepository.getReferenceById(1L)).thenReturn(testRepo);
        when(gitService.getFileStructure("/path/to/repo")).thenReturn(rootNode);

        List<Map<String, Object>> result = fileStructureService.getFileStructure(1L, "root");

        assertThat(result).isNotEmpty();
        verify(repositoryRepository).getReferenceById(1L);
        verify(gitService).getFileStructure("/path/to/repo");
    }

    @Test
    void getFileStructure_withValidRepoAndSubPath_returnsSubtree() throws Exception {
        GitService.Node rootNode = new GitService.Node("root", "", true);
        GitService.Node dirNode = new GitService.Node("src", "src", true);
        GitService.Node fileNode = new GitService.Node("Main.java", "src/Main.java", false);
        dirNode.addChild(fileNode);
        rootNode.addChild(dirNode);

        when(repositoryRepository.getReferenceById(1L)).thenReturn(testRepo);
        when(gitService.getFileStructure("/path/to/repo")).thenReturn(rootNode);

        List<Map<String, Object>> result = fileStructureService.getFileStructure(1L, "src");

        assertThat(result).isNotEmpty();
        verify(repositoryRepository).getReferenceById(1L);
        verify(gitService).getFileStructure("/path/to/repo");
    }

    @Test
    void getFileStructure_withException_returnsEmptyList() throws Exception {
        when(repositoryRepository.getReferenceById(1L)).thenReturn(testRepo);
        when(gitService.getFileStructure("/path/to/repo")).thenThrow(new RuntimeException("Error"));

        List<Map<String, Object>> result = fileStructureService.getFileStructure(1L, "");

        assertThat(result).isEmpty();
        verify(repositoryRepository).getReferenceById(1L);
        verify(gitService).getFileStructure("/path/to/repo");
    }

    }
