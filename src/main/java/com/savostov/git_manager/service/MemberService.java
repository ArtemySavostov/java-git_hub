package com.savostov.git_manager.service;

import com.savostov.git_manager.model.Member;
import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.model.User;
import com.savostov.git_manager.repository.MemberRepository;
import com.savostov.git_manager.repository.RepositoryRepository;
import com.savostov.git_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MemberService {


    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RepositoryRepository repositoryRepository;
    @Autowired
    private UserRepository userRepository;



    public Member addMemberToRepo(Long repoId, Long userId, String role){
        Repo repo = repositoryRepository.findById(repoId).orElseThrow(() -> new RuntimeException("Repository not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Member member = new Member();
        member.setRepo(repo);
        member.setUser(user);
        member.setRole("READ");

        return memberRepository.save(member);
    }
}
