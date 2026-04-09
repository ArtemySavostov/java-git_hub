package com.savostov.git_manager.controller;

import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.repository.RepositoryRepository;
import com.savostov.git_manager.service.FileStructureService;
import com.savostov.git_manager.service.GitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsonRepositoryControllerTest {

    @Mock
    private FileStructureService fileStructureService;

    @Mock
    private GitService gitService;

    @Mock
    private RepositoryRepository repositoryRepository;

    @InjectMocks
    private JsonRepositoryController jsonRepositoryController;

    private Repo testRepo;

    @BeforeEach
    void setUp() {
        testRepo = new Repo();
        testRepo.setId(1L);
        testRepo.setName("test-repo");
        testRepo.setPath("/path/to/repo");
    }

    @Test
    void getFiles_withValidRepo_returnsFileStructure() throws Exception {
        Map<String, Object> fileNode = Map.of("id", "file.txt", "text", "file.txt", "type", "file");
        List<Map<String, Object>> fileStructure = Arrays.asList(fileNode);

        when(fileStructureService.getFileStructure(1L, "")).thenReturn(fileStructure);

        List<Map<String, Object>> result = jsonRepositoryController.getFiles(1L, "");

        assertThat(result).isEqualTo(fileStructure);
        verify(fileStructureService).getFileStructure(1L, "");
    }

    @Test
    void getFiles_withSubPath_returnsSubFileStructure() throws Exception {
        Map<String, Object> fileNode = Map.of("id", "src/Main.java", "text", "Main.java", "type", "file");
        List<Map<String, Object>> fileStructure = Arrays.asList(fileNode);

        when(fileStructureService.getFileStructure(1L, "src")).thenReturn(fileStructure);

        List<Map<String, Object>> result = jsonRepositoryController.getFiles(1L, "src");

        assertThat(result).isEqualTo(fileStructure);
        verify(fileStructureService).getFileStructure(1L, "src");
    }

    @Test
    void getFileContent_withValidRequest_returnsContent() throws Exception {
        when(repositoryRepository.getReferenceById(1L)).thenReturn(testRepo);
        when(gitService.getFileSize("/path/to/repo", "test.txt")).thenReturn(100L);
        when(gitService.getFileContent("/path/to/repo", "test.txt")).thenReturn("file content");

        ResponseEntity<Object> result = jsonRepositoryController.getFileContent(1L, "test.txt");

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        Map<String, Object> responseBody = (Map<String, Object>) result.getBody();
        assertThat(responseBody.get("error")).isEqualTo(false);
        assertThat(responseBody.get("content")).isEqualTo("file content");
    }

    @Test
    void getFileContent_withBinaryFile_returnsBinaryError() throws Exception {
        when(repositoryRepository.getReferenceById(1L)).thenReturn(testRepo);

        ResponseEntity<Object> result = jsonRepositoryController.getFileContent(1L, "image.jpg");

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        Map<String, Object> responseBody = (Map<String, Object>) result.getBody();
        assertThat(responseBody.get("error")).isEqualTo(true);
        assertThat(responseBody.get("type")).isEqualTo("binary");
    }

    @Test
    void getFileContent_withLargeFile_returnsSizeError() throws Exception {
        when(repositoryRepository.getReferenceById(1L)).thenReturn(testRepo);
        when(gitService.getFileSize("/path/to/repo", "large.txt")).thenReturn(2_000_000L);

        ResponseEntity<Object> result = jsonRepositoryController.getFileContent(1L, "large.txt");

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        Map<String, Object> responseBody = (Map<String, Object>) result.getBody();
        assertThat(responseBody.get("error")).isEqualTo(true);
        assertThat(responseBody.get("type")).isEqualTo("too_large");
        assertThat(responseBody.get("size")).isEqualTo(2_000_000L);
    }

    @Test
    void getFileContent_withException_returnsError() throws Exception {
        when(repositoryRepository.getReferenceById(1L)).thenReturn(testRepo);
        when(gitService.getFileSize("/path/to/repo", "test.txt")).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Object> result = jsonRepositoryController.getFileContent(1L, "test.txt");

        assertThat(result.getStatusCodeValue()).isEqualTo(500);
        Map<String, Object> responseBody = (Map<String, Object>) result.getBody();
        assertThat(responseBody.get("error")).isEqualTo(true);
        assertThat(responseBody.get("message")).isNotNull();
    }
}
