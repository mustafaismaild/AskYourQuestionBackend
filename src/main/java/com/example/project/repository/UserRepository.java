package com.example.project.repository;

import com.example.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u left join fetch u.questions q left join fetch u.savedQuestions sq where u.id = :id")
    Optional<User> findByIdWithRelations(@Param("id") Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameAndStatus(String username, boolean status);

    List<User> findByStatus(boolean status);

    @Query("select u from User u where u.username = :username and u.status = true")
    Optional<User> findActiveByUsername(@Param("username") String username);

    @Query("select u from User u where u.email = :email and u.status = true")
    Optional<User> findActiveByEmail(@Param("email") String email);

    @Query("select u from User u where (u.username = :identifier or u.email = :identifier)")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    @Query("select u from User u " +
            "left join fetch u.questions q " +
            "left join fetch u.savedQuestions sq " +
            "where u.username = :username")
    Optional<User> findByUsernameWithRelations(@Param("username") String username);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    List<User> findByUsernameContainingIgnoreCaseAndStatus(String username, boolean status);
}