package com.savostov.git_manager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RepoTest {

    private Repo repo;
    private User owner;

    @BeforeEach
    void setUp() {
        repo = new Repo();
        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");
    }

    @Test
    void repoCreation_withDefaultConstructor_createsEmptyRepo() {
        assertThat(repo.getId()).isNull();
        assertThat(repo.getName()).isNull();
        assertThat(repo.getDescription()).isNull();
        assertThat(repo.getPath()).isNull();
        assertThat(repo.getOwner()).isNull();
        assertThat(repo.isPrivate()).isFalse();
        assertThat(repo.getCreatedAt()).isNull();
        assertThat(repo.getCommits()).isNull();
        assertThat(repo.getBranches()).isNull();
        assertThat(repo.getMembers()).isNull();
    }

    @Test
    void setId_withValidId_setsId() {
        repo.setId(1L);
        assertThat(repo.getId()).isEqualTo(1L);
    }

    @Test
    void setName_withValidName_setsName() {
        repo.setName("test-repo");
        assertThat(repo.getName()).isEqualTo("test-repo");
    }

    @Test
    void setDescription_withValidDescription_setsDescription() {
        repo.setDescription("Test repository description");
        assertThat(repo.getDescription()).isEqualTo("Test repository description");
    }

    @Test
    void setPath_withValidPath_setsPath() {
        repo.setPath("/path/to/repo");
        assertThat(repo.getPath()).isEqualTo("/path/to/repo");
    }

    @Test
    void setOwner_withValidOwner_setsOwner() {
        repo.setOwner(owner);
        assertThat(repo.getOwner()).isEqualTo(owner);
    }

    @Test
    void setPrivate_withTrue_setsPrivate() {
        repo.setPrivate(true);
        assertThat(repo.isPrivate()).isTrue();
    }

    @Test
    void setPrivate_withFalse_setsPublic() {
        repo.setPrivate(false);
        assertThat(repo.isPrivate()).isFalse();
    }

    @Test
    void setCreatedAt_withValidDate_setsCreatedAt() {
        Date date = new Date();
        repo.setCreatedAt(date);
        assertThat(repo.getCreatedAt()).isEqualTo(date);
    }

    @Test
    void setCommits_withValidCommits_setsCommits() {
        List<Commits> commits = List.of(new Commits());
        repo.setCommits(commits);
        assertThat(repo.getCommits()).isEqualTo(commits);
    }

    @Test
    void setBranches_withValidBranches_setsBranches() {
        List<Branch> branches = List.of(new Branch());
        repo.setBranches(branches);
        assertThat(repo.getBranches()).isEqualTo(branches);
    }

    @Test
    void setMembers_withValidMembers_setsMembers() {
        List<Member> members = List.of(new Member());
        repo.setMembers(members);
        assertThat(repo.getMembers()).isEqualTo(members);
    }

    @Test
    void equals_withSameObject_returnsTrue() {
        assertThat(repo.equals(repo)).isTrue();
    }

    @Test
    void equals_withNull_returnsFalse() {
        assertThat(repo.equals(null)).isFalse();
    }

    @Test
    void hashCode_withSameObject_returnsSameHashCode() {
        int hashCode1 = repo.hashCode();
        int hashCode2 = repo.hashCode();
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void toString_withValidRepo_returnsStringRepresentation() {
        repo.setId(1L);
        repo.setName("test-repo");
        repo.setDescription("Test repository");
        repo.setPath("/path/to/repo");
        repo.setOwner(owner);
        repo.setPrivate(false);
        
        String result = repo.toString();
        
        assertThat(result).contains("Repo");
        assertThat(result).contains("id=1");
        assertThat(result).contains("name=test-repo");
        assertThat(result).contains("description=Test repository");
        assertThat(result).contains("path=/path/to/repo");
        assertThat(result).contains("isPrivate=false");
    }
}
