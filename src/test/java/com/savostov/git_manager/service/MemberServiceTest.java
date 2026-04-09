package com.savostov.git_manager.service;

import com.savostov.git_manager.model.Member;
import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.model.User;
import com.savostov.git_manager.repository.MemberRepository;
import com.savostov.git_manager.repository.RepositoryRepository;
import com.savostov.git_manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RepositoryRepository repositoryRepository;

    @InjectMocks
    private MemberService memberService;

    private User testUser;
    private User memberUser;
    private Repo testRepo;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("owner");

        memberUser = new User();
        memberUser.setId(2L);
        memberUser.setUsername("member");

        testRepo = new Repo();
        testRepo.setId(1L);
        testRepo.setName("test-repo");
        testRepo.setOwner(testUser);
    }

    @Test
    void addMemberToRepo_withValidData_addsMember() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(memberUser));
        when(repositoryRepository.findById(1L)).thenReturn(Optional.of(testRepo));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        Member result = memberService.addMemberToRepo(1L, 2L, "developer");

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();
        assertThat(savedMember.getUser()).isEqualTo(memberUser);
        assertThat(savedMember.getRepo()).isEqualTo(testRepo);
        assertThat(savedMember.getRole()).isEqualTo("developer");
        assertThat(result.getUser()).isEqualTo(memberUser);
        assertThat(result.getRepo()).isEqualTo(testRepo);
        assertThat(result.getRole()).isEqualTo("developer");
    }

    @Test
    void addMemberToRepo_withInvalidUserId_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.addMemberToRepo(1L, 999L, "developer"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void addMemberToRepo_withInvalidRepoId_throwsException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(memberUser));
        when(repositoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.addMemberToRepo(999L, 2L, "developer"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Repository not found");

        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void addMemberToRepo_withAdminRole_addsAdminMember() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(memberUser));
        when(repositoryRepository.findById(1L)).thenReturn(Optional.of(testRepo));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        Member result = memberService.addMemberToRepo(1L, 2L, "admin");

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();
        assertThat(savedMember.getRole()).isEqualTo("admin");
        assertThat(result.getRole()).isEqualTo("admin");
    }

    @Test
    void addMemberToRepo_withMaintainerRole_addsMaintainerMember() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(memberUser));
        when(repositoryRepository.findById(1L)).thenReturn(Optional.of(testRepo));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        Member result = memberService.addMemberToRepo(1L, 2L, "maintainer");

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();
        assertThat(savedMember.getRole()).isEqualTo("maintainer");
        assertThat(result.getRole()).isEqualTo("maintainer");
    }

    @Test
    void addMemberToRepo_withNullRole_addsMemberWithNullRole() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(memberUser));
        when(repositoryRepository.findById(1L)).thenReturn(Optional.of(testRepo));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        Member result = memberService.addMemberToRepo(1L, 2L, null);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();
        assertThat(savedMember.getRole()).isNull();
        assertThat(result.getRole()).isNull();
    }
}
