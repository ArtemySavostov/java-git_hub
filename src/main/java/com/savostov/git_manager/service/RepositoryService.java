package com.savostov.git_manager.service;

import com.savostov.git_manager.model.Member;
import com.savostov.git_manager.model.User;
import com.savostov.git_manager.repository.MemberRepository;
import com.savostov.git_manager.repository.RepositoryRepository;
import com.savostov.git_manager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.savostov.git_manager.model.Repo;
import org.springframework.stereotype.Repository;
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
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.Files.isDirectory;

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

    @Autowired
    private MemberRepository memberRepository;

    @Transactional
    public Repo createRepository(User owner, String repositoryName, boolean isPrivate) {
        Repo repository = new Repo();
        repository.setName(repositoryName);
        repository.setOwner(owner);
        repository.setPrivate(isPrivate);
        String path = getUserRootDirectory(owner) + "/" + repositoryName;
        repository.setPath(path);
        repository = repositoryRepository.save(repository);

        Member member = new Member();
        member.setUser(owner);
        member.setRepo(repository);
        member.setRole("ADMIN");
        memberRepository.save(member);
        String userRootDirectory = getUserRootDirectory(owner);
        createRepositoryDirectory(userRootDirectory, repositoryName);
        return repository;
    }

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
        String gitCommand = System.getenv().getOrDefault("GIT_COMMAND", "git");
        ProcessBuilder processBuilder = new ProcessBuilder(gitCommand, "init");
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

    public List<Repo> getRepositoryWithOutPrivate(String username) {
        Optional<User> ownerOptional = userRepository.findByUsername(username);
        if (ownerOptional.isPresent()) {
            User owner = ownerOptional.get();
            List<Repo> repositories = repositoryRepository.findByOwnerId(owner.getId());
            List<Repo> repoWhithoutPrivate = repositories.stream().filter(p -> !p.isPrivate()).collect(Collectors.toList());
            return repoWhithoutPrivate;
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

                String relativePath = file.getOriginalFilename();
                if (relativePath != null) {

                    Path targetPath = repoPath.resolve(relativePath).normalize();


                    if (!targetPath.startsWith(repoPath)) {
                        throw new SecurityException("Invalid file path: " + relativePath);
                    }


                    Files.createDirectories(targetPath.getParent());


                    Files.copy(file.getInputStream(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Uploaded file: " + targetPath);
                }
            }
        }
    }

    public List<Map<String, Object>> getFileStructure(Long repoId, String subPath) {
        Repo repo = repositoryRepository.getReferenceById(repoId);
        Path repoPath = Paths.get(repo.getPath());

        try {
            List<String> items = gitService.getFiles(repoPath.toString()); // Получаем и файлы, и каталоги
            Path resolvedPath = repoPath;
            if (subPath != null && !subPath.isEmpty()) {
                resolvedPath = repoPath.resolve(subPath).normalize();
            }

            Path finalResolvedPath = resolvedPath; // Для использования в лямбда-выражении

            return items.stream().map(itemPath -> {
                Path fullPath = repoPath.resolve(finalResolvedPath).resolve(itemPath).normalize(); // Combine repoPath, subPath, and itemPath
                Path relativePath = repoPath.relativize(fullPath);

                Map<String, Object> node = new HashMap<>();
                node.put("id", relativePath.toString());
                node.put("text", relativePath.getFileName().toString());
                node.put("type", Files.isDirectory(fullPath) ? "directory" : "file");
                return node;
            }).collect(Collectors.toList());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    private boolean isDirectory(Path repoPath, String filePath) {
        Path fullPath = repoPath.resolve(filePath);
        return Files.isDirectory(fullPath);
    }


    public String getFileContentFromGit(Long repoId, String path) throws IOException, InterruptedException {
        Repo repo = repositoryRepository.getReferenceById(repoId);
        String repositoryPath = repo.getPath();
        return gitService.getFileContent(repositoryPath, path);
    }

    public Repo getRepositoryById(Long id){
        return repositoryRepository.getById(id);
    }

    public List<Repo> getListColloborations(Long userId){
        return repositoryRepository.findReposWhereUserIsCollaboratorNotOwner(userId);
    }

}
