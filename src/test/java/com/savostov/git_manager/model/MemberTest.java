package com.savostov.git_manager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    private Member member;
    private User user;
    private Repo repo;

    @BeforeEach
    void setUp() {
        member = new Member();
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        
        repo = new Repo();
        repo.setId(1L);
        repo.setName("test-repo");
    }

    @Test
    void memberCreation_withDefaultConstructor_createsEmptyMember() {
        assertThat(member.getId()).isNull();
        assertThat(member.getUser()).isNull();
        assertThat(member.getRepo()).isNull();
        assertThat(member.getRole()).isNull();
        assertThat(member.getJoinedAt()).isNull();
    }

    @Test
    void setId_withValidId_setsId() {
        member.setId(1L);
        assertThat(member.getId()).isEqualTo(1L);
    }

    @Test
    void setUser_withValidUser_setsUser() {
        member.setUser(user);
        assertThat(member.getUser()).isEqualTo(user);
    }

    @Test
    void setRepo_withValidRepo_setsRepo() {
        member.setRepo(repo);
        assertThat(member.getRepo()).isEqualTo(repo);
    }

    @Test
    void setRole_withValidRole_setsRole() {
        member.setRole("developer");
        assertThat(member.getRole()).isEqualTo("developer");
    }

    @Test
    void setRole_withAdminRole_setsAdminRole() {
        member.setRole("admin");
        assertThat(member.getRole()).isEqualTo("admin");
    }

    @Test
    void setRole_withMaintainerRole_setsMaintainerRole() {
        member.setRole("maintainer");
        assertThat(member.getRole()).isEqualTo("maintainer");
    }

    @Test
    void setRoleWithNull_setsNullRole() {
        member.setRole(null);
        assertThat(member.getRole()).isNull();
    }

    @Test
    void setJoinedAt_withValidDate_setsJoinedAt() {
        Date date = new Date();
        member.setJoinedAt(date);
        assertThat(member.getJoinedAt()).isEqualTo(date);
    }

    @Test
    void equals_withSameObject_returnsTrue() {
        assertThat(member.equals(member)).isTrue();
    }

    @Test
    void equals_withNull_returnsFalse() {
        assertThat(member.equals(null)).isFalse();
    }

    @Test
    void hashCode_withSameObject_returnsSameHashCode() {
        int hashCode1 = member.hashCode();
        int hashCode2 = member.hashCode();
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void toString_withValidMember_returnsStringRepresentation() {
        member.setId(1L);
        member.setUser(user);
        member.setRepo(repo);
        member.setRole("developer");
        
        String result = member.toString();
        
        assertThat(result).contains("Member");
        assertThat(result).contains("id=1");
        assertThat(result).contains("role=developer");
    }
}
