package com.savostov.git_manager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MatchRequestCreateRepoTest {

    private MatchRequestCreateRepo request;

    @BeforeEach
    void setUp() {
        request = new MatchRequestCreateRepo();
    }

    @Test
    void requestCreation_withDefaultConstructor_createsEmptyRequest() {
        assertThat(request.getRepoName()).isNull();
        assertThat(request.getOwner()).isNull();
        assertThat(request.getUserCollaborator()).isNull();
    }

    @Test
    void setRepoName_withValidName_setsRepoName() {
        request.setRepoName("test-repo");
        assertThat(request.getRepoName()).isEqualTo("test-repo");
    }

    @Test
    void setRepoName_withNullName_setsNull() {
        request.setRepoName(null);
        assertThat(request.getRepoName()).isNull();
    }

    @Test
    void setRepoName_withEmptyName_setsEmpty() {
        request.setRepoName("");
        assertThat(request.getRepoName()).isEqualTo("");
    }

    @Test
    void setOwner_withValidOwner_setsOwner() {
        request.setOwner("testowner");
        assertThat(request.getOwner()).isEqualTo("testowner");
    }

    @Test
    void setOwner_withNullOwner_setsNull() {
        request.setOwner(null);
        assertThat(request.getOwner()).isNull();
    }

    @Test
    void setOwner_withEmptyOwner_setsEmpty() {
        request.setOwner("");
        assertThat(request.getOwner()).isEqualTo("");
    }

    @Test
    void setUserCollaborator_withValidCollaborator_setsUserCollaborator() {
        request.setUserCollaborator("collaborator");
        assertThat(request.getUserCollaborator()).isEqualTo("collaborator");
    }

    @Test
    void setUserCollaborator_withNullCollaborator_setsNull() {
        request.setUserCollaborator(null);
        assertThat(request.getUserCollaborator()).isNull();
    }

    @Test
    void setUserCollaborator_withEmptyCollaborator_setsEmpty() {
        request.setUserCollaborator("");
        assertThat(request.getUserCollaborator()).isEqualTo("");
    }

    @Test
    void equals_withSameObject_returnsTrue() {
        assertThat(request.equals(request)).isTrue();
    }

    @Test
    void equals_withNull_returnsFalse() {
        assertThat(request.equals(null)).isFalse();
    }

    @Test
    void equals_withEqualObject_returnsTrue() {
        MatchRequestCreateRepo other = new MatchRequestCreateRepo();
        other.setRepoName("test-repo");
        other.setOwner("testowner");
        other.setUserCollaborator("collaborator");
        
        request.setRepoName("test-repo");
        request.setOwner("testowner");
        request.setUserCollaborator("collaborator");
        
        assertThat(request.equals(other)).isTrue();
    }

    @Test
    void equals_withDifferentRepoName_returnsFalse() {
        MatchRequestCreateRepo other = new MatchRequestCreateRepo();
        other.setRepoName("different-repo");
        other.setOwner("testowner");
        other.setUserCollaborator("collaborator");
        
        request.setRepoName("test-repo");
        request.setOwner("testowner");
        request.setUserCollaborator("collaborator");
        
        assertThat(request.equals(other)).isFalse();
    }

    @Test
    void equals_withDifferentOwner_returnsFalse() {
        MatchRequestCreateRepo other = new MatchRequestCreateRepo();
        other.setRepoName("test-repo");
        other.setOwner("differentowner");
        other.setUserCollaborator("collaborator");
        
        request.setRepoName("test-repo");
        request.setOwner("testowner");
        request.setUserCollaborator("collaborator");
        
        assertThat(request.equals(other)).isFalse();
    }

    @Test
    void equals_withDifferentCollaborator_returnsFalse() {
        MatchRequestCreateRepo other = new MatchRequestCreateRepo();
        other.setRepoName("test-repo");
        other.setOwner("testowner");
        other.setUserCollaborator("differentcollaborator");
        
        request.setRepoName("test-repo");
        request.setOwner("testowner");
        request.setUserCollaborator("collaborator");
        
        assertThat(request.equals(other)).isFalse();
    }

    @Test
    void hashCode_withSameObject_returnsSameHashCode() {
        request.setRepoName("test-repo");
        request.setOwner("testowner");
        request.setUserCollaborator("collaborator");
        
        int hashCode1 = request.hashCode();
        int hashCode2 = request.hashCode();
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void hashCode_withEqualObjects_returnsSameHashCode() {
        MatchRequestCreateRepo other = new MatchRequestCreateRepo();
        other.setRepoName("test-repo");
        other.setOwner("testowner");
        other.setUserCollaborator("collaborator");
        
        request.setRepoName("test-repo");
        request.setOwner("testowner");
        request.setUserCollaborator("collaborator");
        
        assertThat(request.hashCode()).isEqualTo(other.hashCode());
    }

    @Test
    void toString_withValidRequest_returnsStringRepresentation() {
        request.setRepoName("test-repo");
        request.setOwner("testowner");
        request.setUserCollaborator("collaborator");
        
        String result = request.toString();
        
        assertThat(result).contains("MatchRequestCreateRepo");
        assertThat(result).contains("repoName=test-repo");
        assertThat(result).contains("owner=testowner");
        assertThat(result).contains("userCollaborator=collaborator");
    }

    @Test
    void toString_withNullValues_returnsStringRepresentation() {
        request.setRepoName(null);
        request.setOwner(null);
        request.setUserCollaborator(null);
        
        String result = request.toString();
        
        assertThat(result).contains("MatchRequestCreateRepo");
        assertThat(result).contains("repoName=null");
        assertThat(result).contains("owner=null");
        assertThat(result).contains("userCollaborator=null");
    }

    @Test
    void canEqual_withSameClass_returnsTrue() {
        MatchRequestCreateRepo other = new MatchRequestCreateRepo();
        assertThat(request.canEqual(other)).isTrue();
    }

    @Test
    void canEqual_withDifferentClass_returnsFalse() {
        String other = "not a MatchRequestCreateRepo";
        assertThat(request.canEqual(other)).isFalse();
    }
}
