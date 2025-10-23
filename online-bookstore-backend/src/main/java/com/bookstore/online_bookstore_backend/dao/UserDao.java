package com.bookstore.online_bookstore_backend.dao;

import com.bookstore.online_bookstore_backend.entity.User;
import java.util.Optional;
import java.util.List; // Ensure List is imported
// import java.util.List; // Uncomment if findAll or similar list-returning methods are needed

public interface UserDao {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    User save(User user);
    Optional<User> findById(Long id); // Added method to find by ID
    List<User> findAll(); // Added method to find all users
    List<User> findAllById(Iterable<Long> ids); // Add this method
    // Optional<User> findById(Long id); // Uncomment if needed
    // List<User> findAll(); // Uncomment if needed
} 