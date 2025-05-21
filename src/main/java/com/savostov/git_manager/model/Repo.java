package com.savostov.git_manager.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "repositories")
@Data
public class Repo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String description;
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;
    @Column(name = "is_private", nullable = false)
    private boolean isPrivate;

    @OneToMany(mappedBy = "repo")
    private List<Commits> commits;

    @OneToMany(mappedBy = "repo")
    private List<Branch> branches;
    @PrePersist
    protected void onCreate(){
        createdAt = new Date();
    }
    @Column(nullable = false)
    private String path;

}
