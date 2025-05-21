package com.savostov.git_manager.controller;

import com.savostov.git_manager.model.User;
import com.savostov.git_manager.service.RepositoryService;
import com.savostov.git_manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String registerForm(Model model){
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String email, @RequestParam String password, Model model){
        if(username == null || username.isEmpty() || email == null || email.isEmpty() || password == null || password.isEmpty()){
            model.addAttribute("errorMessage", "Заполните все поля");
            return "register";
        }
        if (userService.getUserByUsername(username).isPresent()){
            model.addAttribute("errorMessage", "User already exists");
            return "register";
        }
        userService.createUser(username,email,password);

        return "redirect:/login";
    }
    @GetMapping("/login")
    public  String loginForm(){
        return "login";
    }
}
