package com.savostov.git_manager.repository;


import com.savostov.git_manager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.savostov.git_manager.model.Repo;

import java.util.List;
import java.util.Optional;


@Repository
public interface RepositoryRepository extends JpaRepository<Repo, Long> {
        List<Repo> findByOwnerId(Long id);
        Optional<Repo> getRepositoryByName(String repositoryName);
        @Query("SELECT r FROM Repo r LEFT JOIN Member m ON r = m.repo AND m.user = :user " +
                "WHERE r.owner = :user AND (r.isPrivate = false OR m.user IS NOT NULL)")
        List<Repo> findAccessibleReposByOwnerId(@Param("user") User user);

        @Query("SELECT r FROM Repo r JOIN Member m ON r.id = m.repo.id " +
                "WHERE m.user.id = :userId AND r.owner.id <> :userId")
        List<Repo> findReposWhereUserIsCollaboratorNotOwner(@Param("userId") Long userId);

}
