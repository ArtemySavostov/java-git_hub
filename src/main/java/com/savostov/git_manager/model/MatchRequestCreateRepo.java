package com.savostov.git_manager.model;

import lombok.Data;

@Data
public class MatchRequestCreateRepo {
    private String repoName;
    private String owner;
    private String userCollaborator;
}
