package com.savostov.git_manager.repository;

import com.savostov.git_manager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    public List<User> findUserByUsernameNot(String username);


    public User getByEmail(String email);
    public User getByUsername(String username);
    @Query("SELECT COUNT(s) FROM User u JOIN u.subscribers s WHERE u.id = :userId")
    int countFollowers(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM User u JOIN u.subscriptions s WHERE u.id = :userId")
    int countFollowing(@Param("userId") Long userId);

    @Query("SELECT s.id FROM User u JOIN u.subscriptions s WHERE u.id = :userId")
    List<Long> getFollowingList(@Param("userId") Long userId);
    @Query("SELECT s.id FROM User u JOIN u.subscribers s WHERE u.id = :userId")
    List<Long> getFollowersList(@Param("userId") Long userId);
}
