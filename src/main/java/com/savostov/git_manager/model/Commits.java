package com.savostov.git_manager.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "commits")
@Data
public class Commits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String hash;
    @Column(nullable = false)
    private String message;
    @ManyToOne
    @JoinColumn(name = "repository_id", nullable = false)
    private Repo repo;
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    @Column(name = "commit_date", nullable = false)
    private Date commitDate;
    private String parentHash;

}
