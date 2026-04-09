package com.savostov.git_manager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class CommitsTest {

    private Commits commit;

    @BeforeEach
    void setUp() {
        commit = new Commits();
    }

    @Test
    void commitCreation_withDefaultConstructor_createsEmptyCommit() {
        assertThat(commit.getId()).isNull();
        assertThat(commit.getHash()).isNull();
        assertThat(commit.getMessage()).isNull();
        assertThat(commit.getAuthor()).isNull();
        assertThat(commit.getRepo()).isNull();
        assertThat(commit.getCommitDate()).isNull();
        assertThat(commit.getParentHash()).isNull();
    }

    @Test
    void setId_withValidId_setsId() {
        commit.setId(1L);
        assertThat(commit.getId()).isEqualTo(1L);
    }

    @Test
    void setHash_withValidHash_setsHash() {
        String hash = "abc123def456";
        commit.setHash(hash);
        assertThat(commit.getHash()).isEqualTo(hash);
    }

    @Test
    void setHash_withShortHash_setsHash() {
        String hash = "abc123";
        commit.setHash(hash);
        assertThat(commit.getHash()).isEqualTo(hash);
    }

    @Test
    void setMessage_withValidMessage_setsMessage() {
        String message = "Initial commit";
        commit.setMessage(message);
        assertThat(commit.getMessage()).isEqualTo(message);
    }

    @Test
    void setMessage_withLongMessage_setsMessage() {
        String message = "This is a very long commit message that describes all the changes made in this particular commit";
        commit.setMessage(message);
        assertThat(commit.getMessage()).isEqualTo(message);
    }

    @Test
    void setAuthor_withValidAuthor_setsAuthor() {
        User author = new User();
        author.setId(1L);
        author.setUsername("johndoe");
        
        commit.setAuthor(author);
        assertThat(commit.getAuthor()).isEqualTo(author);
    }

    @Test
    void setRepo_withValidRepo_setsRepo() {
        Repo repo = new Repo();
        repo.setId(1L);
        repo.setName("test-repo");
        
        commit.setRepo(repo);
        assertThat(commit.getRepo()).isEqualTo(repo);
    }

    @Test
    void setCommitDate_withValidDate_setsCommitDate() {
        Date date = new Date();
        commit.setCommitDate(date);
        assertThat(commit.getCommitDate()).isEqualTo(date);
    }

    @Test
    void setCommitDate_withPastDate_setsCommitDate() {
        Date date = new Date(1672574400000L); // 2023-01-01 12:00:00 UTC
        commit.setCommitDate(date);
        assertThat(commit.getCommitDate()).isEqualTo(date);
    }

    @Test
    void setParentHash_withValidParentHash_setsParentHash() {
        String parentHash = "def456abc789";
        commit.setParentHash(parentHash);
        assertThat(commit.getParentHash()).isEqualTo(parentHash);
    }

    @Test
    void equals_withSameObject_returnsTrue() {
        assertThat(commit.equals(commit)).isTrue();
    }

    @Test
    void equals_withNull_returnsFalse() {
        assertThat(commit.equals(null)).isFalse();
    }

    @Test
    void hashCode_withSameObject_returnsSameHashCode() {
        int hashCode1 = commit.hashCode();
        int hashCode2 = commit.hashCode();
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void toString_withValidCommit_returnsStringRepresentation() {
        Repo repo = new Repo();
        repo.setId(1L);
        repo.setName("test-repo");
        
        User author = new User();
        author.setId(1L);
        author.setUsername("johndoe");
        
        Date date = new Date(1672574400000L);
        
        commit.setId(1L);
        commit.setHash("abc123def456");
        commit.setMessage("Initial commit");
        commit.setAuthor(author);
        commit.setRepo(repo);
        commit.setCommitDate(date);
        commit.setParentHash("def456abc789");
        
        String result = commit.toString();
        
        assertThat(result).contains("Commits");
        assertThat(result).contains("id=1");
        assertThat(result).contains("hash=abc123def456");
        assertThat(result).contains("message=Initial commit");
    }
}
