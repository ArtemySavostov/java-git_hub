package com.savostov.git_manager.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.savostov.git_manager.model.Repo;

import java.util.List;
import java.util.Optional;


@Repository
public interface RepositoryRepository extends JpaRepository<Repo, Long> {
        List<Repo> findByOwnerId(Long id);
        Optional<Repo> getRepositoryByName(String repositoryName);

}
