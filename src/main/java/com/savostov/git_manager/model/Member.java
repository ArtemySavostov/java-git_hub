package com.savostov.git_manager.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.repository.Repository;

import java.util.Date;

@Entity
@Table(name = "members")
@Data
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id", nullable = false)
    private Repo repo;


    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Date joinedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = new Date();
    }

}
