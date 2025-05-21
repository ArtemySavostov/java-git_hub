package com.savostov.git_manager.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "branches")
@Data
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @ManyToOne
    @JoinColumn(name = "repository_id", nullable = false)
    private Repo repo;
    @ManyToOne
    @JoinColumn(name = "head_commit_id")
    private Commits headCommit;
}
