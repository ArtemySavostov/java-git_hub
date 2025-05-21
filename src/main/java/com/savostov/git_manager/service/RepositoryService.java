package com.savostov.git_manager.service;

import com.savostov.git_manager.model.User;
import com.savostov.git_manager.repository.RepositoryRepository;
import com.savostov.git_manager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.savostov.git_manager.model.Repo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class RepositoryService {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryService.class);
    @Autowired
    private RepositoryRepository repositoryRepository;

    @Value("${repositories.path}")
    private String repositoriesPath;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GitService gitService;

    @Transactional
    public Repo createRepository(User owner, String repositoryName, boolean isPrivate) {
        Repo repository = new Repo();
        repository.setName(repositoryName);
        repository.setOwner(owner);
        repository.setPrivate(isPrivate);
        String path = getUserRootDirectory(owner) + "/" + repositoryName;
        repository.setPath(path);
        repository = repositoryRepository.save(repository);

        String userRootDirectory = getUserRootDirectory(owner);
        createRepositoryDirectory(userRootDirectory, repositoryName);
        return repository;
    }

    // ДОБАВИТЬ ПРОВЕРКУ СУЩЕСТВУЕТ ЛИ РЕПО!!!!!!!!!!!!
    private void createRepositoryDirectory(String userRootDirectory, String repositoryName) {

        String repositoryPath = userRootDirectory + "/" + repositoryName;
        try {
            Path repoPath = Paths.get(repositoryPath);
            Files.createDirectories(repoPath);

            initializeGitRepository(repositoryPath);
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to create repository directory: " + e.getMessage());
            throw new RuntimeException("Failed to create repository directory", e);
        }
    }

    private void initializeGitRepository(String repositoryPath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/git", "init");
        processBuilder.directory(new File(repositoryPath));
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("git init failed with exit code " + exitCode);
        }
        System.out.println("Git repository initialized successfully in " + repositoryPath);
    }

    public String getUserRootDirectory(User owner) {
        String userRootDirectory = repositoriesPath + "/" + owner.getUsername();
        try {
            Path path = Paths.get(userRootDirectory);
            Files.createDirectories(path);
        } catch (IOException e) {
            System.err.println("Failed to create repository directory: " + e.getMessage());
            throw new RuntimeException("Failed to create repository directory", e);
        }
        return userRootDirectory;
    }

    @Transactional
    public Repo createDefaultRepository(User user) {
        String repositoryName = user.getUsername() + "-repository";
        Repo repository = new Repo();
        repository.setName(repositoryName);
        repository.setOwner(user);
        repository.setPrivate(false);
        repositoryRepository.save(repository);
        String userRootDirectory = getUserRootDirectory(user);
        createRepositoryDirectory(userRootDirectory, repositoryName);
        return repository;
    }

    public List<Repo> getRepositoryByOwnerId(String username) {
        Optional<User> ownerOptional = userRepository.findByUsername(username);
        if (ownerOptional.isPresent()) {
            User owner = ownerOptional.get();
            List<Repo> repositories = repositoryRepository.findByOwnerId(owner.getId());
            logger.info("Found repositories: {}", repositories);
            return repositories;
        } else {

            return List.of();
        }

    }

    public Optional<Repo> getRepositoryByName(String repositoryName) {
        Optional<Repo> repository = repositoryRepository.getRepositoryByName(repositoryName);
        return repository;
    }

    public void uplodFiles(Long id, MultipartFile[] files) throws IOException {
        Repo repo = repositoryRepository.getReferenceById(id);
        Path repoPath = Paths.get(repo.getPath());
        if (!Files.exists(repoPath)) {
            Files.createDirectories(repoPath);
        }
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                Path filePath = repoPath.resolve(file.getOriginalFilename());
                Files.copy(file.getInputStream(), filePath);
                System.out.println("Uploaded " + filePath.toString());
            }
        }
    }
}
