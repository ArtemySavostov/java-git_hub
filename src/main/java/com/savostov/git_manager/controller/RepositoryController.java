package com.savostov.git_manager.controller;

import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.model.User;
import com.savostov.git_manager.repository.RepositoryRepository;
import com.savostov.git_manager.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.header.Header;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Controller
public class RepositoryController {
    @Autowired
    private GitService gitService;

    @Autowired
    private RepositoryRepository repositoryRepository;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private FileStructureService fileStructureService;

    @Autowired
    private UserService userService;
    @Autowired
    private MemberService memberService;

    @Value("${repositories.path}")
    private String repositoriesPath;


    @GetMapping("/repository/{id}")
    public String showRepository(@PathVariable Long id,
                                 @RequestParam(value = "path", required = false, defaultValue = "") String path,
                                 Model model) throws ChangeSetPersister.NotFoundException, IOException, InterruptedException {

        Repo repository = repositoryRepository.findById(id)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);


        model.addAttribute("repo", repository);
        model.addAttribute("currentPath", path);

        return "repository";
    }

    @GetMapping("/repositories")
    public String showListRepositories(Model model) {
        List<Repo> repos = repositoryRepository.findAll();
        model.addAttribute("repo", repos);
        return "repositories_list";
    }


    @GetMapping("/repository/{id}/file/**")
    public String showFiles(@PathVariable Long id, HttpServletRequest request, Model model) throws ChangeSetPersister.NotFoundException, IOException, InterruptedException {
        String filePath = extractFilePath(request, "/repository/" + id + "/file/");
        String decodedFilePath = UriUtils.decode(filePath, StandardCharsets.UTF_8);
        System.out.println("Extracted filePath: " + decodedFilePath);
        Repo repository = repositoryRepository.findById(id)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
        String content = gitService.getFileContent(repository.getPath(), filePath);
        model.addAttribute("repo", repository);
        model.addAttribute("filePath", filePath);
        model.addAttribute("content", content);
        return "file";
    }

    private String extractFilePath(HttpServletRequest request, String prefix) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.substring(prefix.length());
    }

    @PostMapping("/repository/{id}/upload")
    public String loadFiles(@PathVariable Long id, @RequestParam("files") MultipartFile[] files, Model model) throws IOException, InterruptedException {
        Repo repo = repositoryRepository.getReferenceById(id);
        String commitMessage = "Added new files";
        repositoryService.uplodFiles(id, files);
        gitService.addFiles(repo.getPath());
        gitService.commitFiles(repo.getPath(), commitMessage);
        return "redirect:/repository/" + id;

    }


    @GetMapping("/fileContent")
    public ResponseEntity<String> getFileContent(@PathVariable Long repoId, @RequestParam String path) {
        try {
            String content = repositoryService.getFileContentFromGit(repoId, path);
            return ResponseEntity.ok(content);
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found or error reading content");
        }
    }


    @GetMapping("/repository/members")
    public String collaborations(@PathVariable Long repoId, Model model) {
        List<User> userList = userService.getAllUsers();
        model.addAttribute(userList);
        return "repository_members";
    }

    @PostMapping("/repository/members/add")
    public void addMemberToRepository(@PathVariable Long repoId, @PathVariable Long userId, @PathVariable String role) {

        memberService.addMemberToRepo(repoId, userId, role);

    }


}
