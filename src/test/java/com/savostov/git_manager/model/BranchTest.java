package com.savostov.git_manager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BranchTest {

    private Branch branch;

    @BeforeEach
    void setUp() {
        branch = new Branch();
    }

    @Test
    void branchCreation_withDefaultConstructor_createsEmptyBranch() {
        assertThat(branch.getId()).isNull();
        assertThat(branch.getName()).isNull();
        assertThat(branch.getRepo()).isNull();
        assertThat(branch.getHeadCommit()).isNull();
    }

    @Test
    void setId_withValidId_setsId() {
        branch.setId(1L);
        assertThat(branch.getId()).isEqualTo(1L);
    }

    @Test
    void setName_withValidName_setsName() {
        branch.setName("main");
        assertThat(branch.getName()).isEqualTo("main");
    }

    @Test
    void setName_withFeatureBranchName_setsName() {
        branch.setName("feature/new-feature");
        assertThat(branch.getName()).isEqualTo("feature/new-feature");
    }

    @Test
    void setRepo_withValidRepo_setsRepo() {
        Repo repo = new Repo();
        repo.setId(1L);
        repo.setName("test-repo");
        
        branch.setRepo(repo);
        assertThat(branch.getRepo()).isEqualTo(repo);
    }

    @Test
    void setHeadCommit_withValidCommit_setsHeadCommit() {
        Commits commit = new Commits();
        commit.setId(1L);
        commit.setHash("abc123");
        
        branch.setHeadCommit(commit);
        assertThat(branch.getHeadCommit()).isEqualTo(commit);
    }

    @Test
    void equals_withSameObject_returnsTrue() {
        assertThat(branch.equals(branch)).isTrue();
    }

    @Test
    void equals_withNull_returnsFalse() {
        assertThat(branch.equals(null)).isFalse();
    }

    @Test
    void hashCode_withSameObject_returnsSameHashCode() {
        int hashCode1 = branch.hashCode();
        int hashCode2 = branch.hashCode();
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void toString_withValidBranch_returnsStringRepresentation() {
        Repo repo = new Repo();
        repo.setId(1L);
        repo.setName("test-repo");
        
        Commits commit = new Commits();
        commit.setId(1L);
        commit.setHash("abc123");
        
        branch.setId(1L);
        branch.setName("main");
        branch.setRepo(repo);
        branch.setHeadCommit(commit);
        
        String result = branch.toString();
        
        assertThat(result).contains("Branch");
        assertThat(result).contains("id=1");
        assertThat(result).contains("name=main");
    }
}
