package com.savostov.git_manager.controller;

import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.model.User;
import com.savostov.git_manager.repository.UserRepository;
import com.savostov.git_manager.service.RepositoryService;
import com.savostov.git_manager.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user/{id}")
    public String getUserById(@PathVariable Long id, Model model) {
       
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Repo> repoList = repositoryService.getRepositoryWithOutPrivate(user.getUsername());
        int followers = userRepository.countFollowers(id);
        int following  = userRepository.countFollowing(id);
        model.addAttribute("followersCount", followers);
        model.addAttribute("followingCount", following);
        model.addAttribute("repositories", repoList);
        model.addAttribute("user", user);

        return "user_page";
    }


    @GetMapping("/users")
    public String listUsers(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        List<User> users = userService.findUserByUsernameNot(username);
        User currentUser = userService.getUserByUsername(username).orElse(null);
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        List<Long> followingList = userRepository.getFollowingList(currentUser.getId());
        model.addAttribute("followingUsers", followingList);
        model.addAttribute("users", users);
        model.addAttribute("currentUserId", currentUserId);
        return "user_list";
    }

    @PostMapping("{subscriberId}/subscribe/{subscribedToId}")
    public ResponseEntity<String> subscribe(@PathVariable Long subscriberId, @PathVariable Long subscribedToId, Model model){
        userService.subscriber(subscriberId, subscribedToId);
        return ResponseEntity.ok("Subscribed successfully");
    }

    @PostMapping("{subscriberId}/unsubscribe/{subscribedToId}")
    public ResponseEntity<String> unsubscribe(@PathVariable Long subscriberId, @PathVariable Long subscribedToId){
        userService.unsubscribe(subscriberId, subscribedToId);
        return ResponseEntity.ok("Unsubscribed successfully");
    }

}
