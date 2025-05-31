package com.savostov.git_manager.service;

import com.savostov.git_manager.model.User;
import com.savostov.git_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RepositoryService repositoryService;

    @Transactional
    public void createUser(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setRole("USER");
        user.setPassword(password);
        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        userRepository.save(user);
        //repositoryService.createDefaultRepository(user);
        repositoryService.getUserRootDirectory(user);
        // repositoryService.createDefaultRepository(user);
    }

    public Optional<User> findUserById(long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void saveUser(User newUser) {
        userRepository.save(newUser);
    }

    @Transactional
    public void subscriber(Long subscriberId, Long subscriberToId) {
        User subscriber = userRepository.findById(subscriberId)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));
        User subscriberedTo = userRepository.findById(subscriberToId)
                .orElseThrow(() -> new RuntimeException("User to subscribe not found"));
        subscriber.getSubscriptions().add(subscriberedTo);
        userRepository.save(subscriber);
    }

    public void unsubscribe(Long subscriberId, Long subscriberToId) {
        User subscriber = userRepository.findById(subscriberId)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));
        User subscriberedTo = userRepository.findById(subscriberToId)
                .orElseThrow(() -> new RuntimeException("User to subscribe not found"));
        subscriber.getSubscriptions().remove(subscriberedTo);
        userRepository.save(subscriber);
    }


    public List<User> findUserByUsernameNot(String username) {
        return userRepository.findUserByUsernameNot(username);
    }

    public int getFollowingCount(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getSubscribers().size())
                .orElse(0);
    }

    public int getFollowersCount(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getSubscriptions().size())
                .orElse(0);
    }
}
