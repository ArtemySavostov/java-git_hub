package com.savostov.git_manager.controller;

import com.savostov.git_manager.model.MatchRequestCreateRepo;
import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.model.User;
import com.savostov.git_manager.repository.RepositoryRepository;
import com.savostov.git_manager.repository.UserRepository;
import com.savostov.git_manager.service.MemberService;
import com.savostov.git_manager.service.RepositoryService;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.nio.file.Path;
import java.util.Optional;

@Controller
public class RequestController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    private RepositoryRepository repositoryRepository;

    @Autowired
    private MemberService memberService;
    @Autowired
    private RepositoryService repositoryService;

    private final String apiSecretKey = "secret";


    @PostMapping("/repo/createWithTeammate")
    public ResponseEntity<String> createRepo(@RequestBody MatchRequestCreateRepo matchRequestCreateRepo,
                                             @RequestHeader("X-API-KEY") String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("API Key header is missing");
        }

        if (!apiKey.equals(apiSecretKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API Key");
        }
        try {
            User userOwner = userRepository.getByUsername(matchRequestCreateRepo.getOwner());
            User userCollaborator = userRepository.getByUsername(matchRequestCreateRepo.getUserCollaborator());

            if (userOwner == null || userCollaborator == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Owner or Collaborator email not found");
            }

            Repo repo = repositoryService.createRepository(userOwner, matchRequestCreateRepo.getRepoName(), false);

            memberService.addMemberToRepo(repo.getId(), userCollaborator.getId(), "READ");
            return ResponseEntity.ok("Repository created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

}
