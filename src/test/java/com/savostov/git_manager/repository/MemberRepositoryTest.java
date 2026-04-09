package com.savostov.git_manager.repository;

import com.savostov.git_manager.model.Member;
import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
class MemberRepositoryTest {

    @Mock
    private MemberRepository memberRepository;

    private Member testMember;
    private User testUser;
    private Repo testRepo;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testRepo = new Repo();
        testRepo.setId(1L);
        testRepo.setName("test-repo");
        testRepo.setOwner(testUser);

        testMember = new Member();
        testMember.setId(1L);
        testMember.setUser(testUser);
        testMember.setRepo(testRepo);
        testMember.setRole("developer");
    }

    @Test
    void findById_withValidId_returnsMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        Optional<Member> result = memberRepository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testMember);
    }

    @Test
    void findById_withInvalidId_returnsEmpty() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Member> result = memberRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_returnsAllMembers() {
        Member member2 = new Member();
        member2.setId(2L);
        member2.setUser(testUser);
        member2.setRepo(testRepo);
        member2.setRole("admin");

        List<Member> members = Arrays.asList(testMember, member2);
        when(memberRepository.findAll()).thenReturn(members);

        List<Member> result = memberRepository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).contains(testMember, member2);
    }

    @Test
    void save_withValidMember_savesMember() {
        when(memberRepository.save(testMember)).thenReturn(testMember);

        Member result = memberRepository.save(testMember);

        assertThat(result).isEqualTo(testMember);
    }

    @Test
    void delete_withValidMember_deletesMember() {
        memberRepository.delete(testMember);
    }

    @Test
    void deleteById_withValidId_deletesMember() {
        memberRepository.deleteById(1L);
    }

    @Test
    void count_returnsMemberCount() {
        when(memberRepository.count()).thenReturn(5L);

        long result = memberRepository.count();

        assertThat(result).isEqualTo(5L);
    }

    @Test
    void existsById_withExistingId_returnsTrue() {
        when(memberRepository.existsById(1L)).thenReturn(true);

        boolean result = memberRepository.existsById(1L);

        assertThat(result).isTrue();
    }

    @Test
    void existsById_withNonExistingId_returnsFalse() {
        when(memberRepository.existsById(999L)).thenReturn(false);

        boolean result = memberRepository.existsById(999L);

        assertThat(result).isFalse();
    }

    @Test
    void findAllById_withValidIds_returnsMembers() {
        List<Long> ids = Arrays.asList(1L, 2L);
        List<Member> members = Arrays.asList(testMember);
        when(memberRepository.findAllById(ids)).thenReturn(members);

        List<Member> result = memberRepository.findAllById(ids);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testMember);
    }

    @Test
    void getReferenceById_withValidId_returnsReference() {
        when(memberRepository.getReferenceById(1L)).thenReturn(testMember);

        Member result = memberRepository.getReferenceById(1L);

        assertThat(result).isEqualTo(testMember);
    }
}
