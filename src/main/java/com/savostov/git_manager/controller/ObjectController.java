package com.savostov.git_manager.controller;

import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.model.User;
import com.savostov.git_manager.service.RepositoryService;
import com.savostov.git_manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ObjectController {

    @Autowired
    private UserService userService;
    @Autowired
    private RepositoryService repositoryService;
    @GetMapping("/api/repo")
    public ResponseEntity<Repo> getRepoObject() {
        Repo repo = new Repo();
        return ResponseEntity.ok(repo);
    }
    @PostMapping("/api/new")
    public  ResponseEntity<Repo> createRepo(Repo repo){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        repositoryService.createRepository(user, repo.getName(), repo.isPrivate());
        return ResponseEntity.ok(repo);
    }

}
