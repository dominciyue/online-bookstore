package com.bookstore.online_bookstore_backend.repository;

import com.bookstore.online_bookstore_backend.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    // Optional: Add custom query methods if needed in the future, 
    // e.g., Optional<UserAuth> findByUserId(Long userId);
    // However, with @MapsId, fetching UserAuth by User's ID is same as fetching by its own ID.
} 