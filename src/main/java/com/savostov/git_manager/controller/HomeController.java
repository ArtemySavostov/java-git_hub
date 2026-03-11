package com.savostov.git_manager.controller;

import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.model.User;
import com.savostov.git_manager.repository.UserRepository;
import com.savostov.git_manager.service.RepositoryService;
import com.savostov.git_manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private UserRepository userRepository;
    @GetMapping("/home")
    public String homePage(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> user = userService.getUserByUsername(username);
        User user1 = userRepository.findByUsername(username).orElseThrow();
        List<Repo> repoList = repositoryService.getRepositoryByOwnerId(username);
        int followers = userRepository.countFollowers(user1.getId());
        int following  = userRepository.countFollowing(user1.getId());
        model.addAttribute("followersCount", followers);
        model.addAttribute("followingCount", following);
        model.addAttribute("repositories", repoList);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
        }
        return "home";
    }

    @GetMapping("/repo/new")
    public String newRepositoryForm(Model model){
        model.addAttribute("repo", new Repo());
        return "repositories_form";
    }




    @PostMapping("/repo/create")
    public String createRepository(@ModelAttribute("repo") Repo repository){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        repositoryService.createRepository(user, repository.getName(), repository.isPrivate());
        return "redirect:/home";
    }

    @GetMapping("/repo/{id}/edit")
    public String editRepositoryForm(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Repo repo = repositoryService.getRepositoryById(id);
        if (repo == null || repo.getOwner() == null || !repo.getOwner().getUsername().equals(username)) {
            return "redirect:/home";
        }

        model.addAttribute("repo", repo);
        return "repository_edit";
    }

    @PostMapping("/repo/{id}/edit")
    public String updateRepository(@PathVariable Long id, @ModelAttribute("repo") Repo repositoryForm) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Repo existing = repositoryService.getRepositoryById(id);
        if (existing == null || existing.getOwner() == null || !existing.getOwner().getUsername().equals(username)) {
            return "redirect:/home";
        }

        repositoryService.updateRepository(
                id,
                repositoryForm.getName(),
                repositoryForm.getDescription(),
                repositoryForm.isPrivate()
        );

        return "redirect:/home";
    }

    @PostMapping("/repo/{id}/delete")
    public String deleteRepository(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Repo existing = repositoryService.getRepositoryById(id);
        if (existing == null || existing.getOwner() == null || !existing.getOwner().getUsername().equals(username)) {
            return "redirect:/home";
        }

        repositoryService.deleteRepository(id);
        return "redirect:/home";
    }
}
