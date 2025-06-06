package com.savostov.git_manager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;


import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {
    public User() {

    }

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @ManyToMany
    @JoinTable(
            name = "user_subscription",
            joinColumns = @JoinColumn(name = "subscriber_id"),
            inverseJoinColumns = @JoinColumn(name = "subscriber_to_id")
    )
    private Set<User> subscriptions = new HashSet<>();
    @ManyToMany(mappedBy = "subscriptions")
    private Set<User> subscribers = new HashSet<>();
    @Column(name = "role")
    private String role;
//    @Column(nullable = false)
//    private String publicKey;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
