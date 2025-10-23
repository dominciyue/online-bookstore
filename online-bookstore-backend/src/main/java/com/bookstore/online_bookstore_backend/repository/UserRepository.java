package com.bookstore.online_bookstore_backend.repository;

import com.bookstore.online_bookstore_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userAuth WHERE u.username = :username")
    Optional<User> findByUsernameWithUserAuth(@Param("username") String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
} 