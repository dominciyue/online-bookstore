package com.bookstore.online_bookstore_backend.dao;

import com.bookstore.online_bookstore_backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderDao {
    Order save(Order order);
    Optional<Order> findById(Long id);

    // Original method - keep if used, but prefer paginated version for lists
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    // Paginated and filtered methods for User
    Page<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);
    Page<Order> findByUserIdAndOrderDateBetweenOrderByOrderDateDesc(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Order> findByUserIdAndBookNameContainingIgnoreCase(Long userId, String bookNameKeyword, Pageable pageable);
    Page<Order> findByUserIdAndOrderDateBetweenAndBookNameContainingIgnoreCase(Long userId, LocalDateTime startDate, LocalDateTime endDate, String bookNameKeyword, Pageable pageable);

    // Paginated and filtered methods for Admin
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);
    Page<Order> findAllByOrderDateBetweenOrderByOrderDateDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Order> findAllByBookNameContainingIgnoreCase(String bookNameKeyword, Pageable pageable);
    Page<Order> findAllByOrderDateBetweenAndBookNameContainingIgnoreCase(LocalDateTime startDate, LocalDateTime endDate, String bookNameKeyword, Pageable pageable);
    Page<Order> findByUserIdAndBookNameContainingIgnoreCaseForAdmin(Long userId, String bookNameKeyword, Pageable pageable);
    Page<Order> findByUserIdAndOrderDateBetweenAndBookNameContainingIgnoreCaseForAdmin(Long userId, LocalDateTime startDate, LocalDateTime endDate, String bookNameKeyword, Pageable pageable);
    
    // Book name search will be added later if feasible via DAO or service layer logic
} 