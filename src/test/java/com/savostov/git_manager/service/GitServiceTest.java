package com.savostov.git_manager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class GitServiceTest {

    @InjectMocks
    private GitService gitService;

    @TempDir
    Path tempDir;

    private String testRepoPath;

    @BeforeEach
    void setUp() throws IOException {
        testRepoPath = tempDir.toString() + "/test-repo";
        Files.createDirectories(Path.of(testRepoPath));
        
        // Create some test files
        Files.createFile(Path.of(testRepoPath, "test.txt"));
        Files.createFile(Path.of(testRepoPath, "README.md"));
        Files.createDirectories(Path.of(testRepoPath, "src"));
        Files.createFile(Path.of(testRepoPath, "src/Main.java"));
    }

    @Test
    void getFileStructure_withValidRepo_returnsFileStructure() throws Exception {
        GitService.Node result = gitService.getFileStructure(testRepoPath);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("");
        assertThat(result.isDirectory()).isTrue();
        assertThat(result.getChildren()).isNotEmpty();
    }

    @Test
    void getFileContent_withValidFile_returnsContent() throws Exception {
        Path testFile = Path.of(testRepoPath, "test.txt");
        Files.write(testFile, "Test content".getBytes());

        String content = gitService.getFileContent(testRepoPath, "test.txt");

        assertThat(content).isNotNull();
    }

    @Test
    void getFileContent_withNonExistentFile_throwsException() {
        assertThatThrownBy(() -> gitService.getFileContent(testRepoPath, "nonexistent.txt"))
                .isInstanceOf(IOException.class);
    }

    @Test
    void addFiles_withValidRepo_addsFiles() throws Exception {
        gitService.addFiles(testRepoPath);
        // If no exception is thrown, the test passes
    }

    @Test
    void commitFiles_withValidRepo_commitsFiles() throws Exception {
        gitService.commitFiles(testRepoPath, "Test commit");
        // If no exception is thrown, the test passes
    }

    @Test
    void getFiles_withValidRepo_returnsFileList() throws Exception {
        List<String> files = gitService.getFiles(testRepoPath);

        assertThat(files).isNotEmpty();
        assertThat(files).contains("test.txt", "README.md");
    }

    @Test
    void getSubtreeAsList_withEmptyPath_returnsAllFiles() throws Exception {
        List<GitService.Node> result = gitService.getSubtreeAsList(testRepoPath, "");

        assertThat(result).isNotNull();
    }

    @Test
    void getSubtreeAsList_withRootPath_returnsAllFiles() throws Exception {
        List<GitService.Node> result = gitService.getSubtreeAsList(testRepoPath, "root");

        assertThat(result).isNotNull();
    }

    @Test
    void getSubtreeAsList_withValidSubPath_returnsSubtree() throws Exception {
        List<GitService.Node> result = gitService.getSubtreeAsList(testRepoPath, "src");

        assertThat(result).isNotNull();
    }

    @Test
    void getSubtreeAsList_withInvalidPath_returnsEmptyList() throws Exception {
        List<GitService.Node> result = gitService.getSubtreeAsList(testRepoPath, "invalid");

        assertThat(result).isEmpty();
    }

    @Test
    void getFileSize_withValidFile_returnsSize() throws Exception {
        Path testFile = Path.of(testRepoPath, "test.txt");
        Files.write(testFile, "Test content".getBytes());

        long size = gitService.getFileSize(testRepoPath, "test.txt");

        assertThat(size).isGreaterThan(0);
    }

    @Test
    void getFileSize_withNonExistentFile_throwsException() {
        assertThatThrownBy(() -> gitService.getFileSize(testRepoPath, "nonexistent.txt"))
                .isInstanceOf(IOException.class);
    }

    @Test
    void Node_creation_createsNodeCorrectly() {
        GitService.Node node = new GitService.Node("test", "test/path", false);

        assertThat(node.getName()).isEqualTo("test");
        assertThat(node.getPath()).isEqualTo("test/path");
        assertThat(node.isDirectory()).isFalse();
        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    void Node_directoryNode_createsDirectoryCorrectly() {
        GitService.Node node = new GitService.Node("dir", "dir/path", true);

        assertThat(node.getName()).isEqualTo("dir");
        assertThat(node.getPath()).isEqualTo("dir/path");
        assertThat(node.isDirectory()).isTrue();
    }

    @Test
    void Node_addChild_addsChildCorrectly() {
        GitService.Node parent = new GitService.Node("parent", "parent", true);
        GitService.Node child = new GitService.Node("child", "parent/child", false);

        parent.addChild(child);

        assertThat(parent.getChildren()).contains(child);
    }

    @Test
    void Node_setDirectory_setsDirectoryCorrectly() {
        GitService.Node node = new GitService.Node("test", "test", false);

        node.setDirectory(true);

        assertThat(node.isDirectory()).isTrue();
    }
}
