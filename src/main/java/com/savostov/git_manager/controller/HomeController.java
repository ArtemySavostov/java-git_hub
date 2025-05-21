package com.savostov.git_manager.controller;

import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.model.User;
import com.savostov.git_manager.service.RepositoryService;
import com.savostov.git_manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/repo")
public class HomeController {

    @Autowired
    private UserService userService;
    @Autowired
    private RepositoryService repositoryService;
    @GetMapping("/home")
    public String homePage(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<Repo> repoList = repositoryService.getRepositoryByOwnerId(username);
        model.addAttribute("repositories", repoList);
        return "home";
    }
    @GetMapping("/new")
    public String newRepositoryForm(Model model){
        model.addAttribute("repo", new Repo());
        return "repositories_form";
    }

    @PostMapping("/create")
    public String createRepository(@ModelAttribute("repo") Repo repository){
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    User user = userService.getUserByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
    repositoryService.createRepository(user, repository.getName(), false);
    return "redirect:/repo/home";
    }
}
