package com.bookstore.online_bookstore_backend.repository;

import com.bookstore.online_bookstore_backend.entity.Order;
import com.bookstore.online_bookstore_backend.payload.response.UserConsumptionStatsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    // User-specific queries
    Page<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);
    Page<Order> findByUserIdAndOrderDateBetweenOrderByOrderDateDesc(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.book b WHERE o.userId = :userId AND LOWER(b.title) LIKE LOWER(CONCAT('%', :bookNameKeyword, '%'))")
    Page<Order> findByUserIdAndBookNameContainingIgnoreCase(@Param("userId") Long userId, @Param("bookNameKeyword") String bookNameKeyword, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.book b WHERE o.userId = :userId AND o.orderDate BETWEEN :startDate AND :endDate AND LOWER(b.title) LIKE LOWER(CONCAT('%', :bookNameKeyword, '%'))")
    Page<Order> findByUserIdAndOrderDateBetweenAndBookNameContainingIgnoreCase(
            @Param("userId") Long userId, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate, 
            @Param("bookNameKeyword") String bookNameKeyword, 
            Pageable pageable);

    // Admin-specific queries (can filter by all, date range, book name, or combinations)
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);
    Page<Order> findAllByOrderDateBetweenOrderByOrderDateDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :bookNameKeyword, '%'))")
    Page<Order> findAllByBookNameContainingIgnoreCase(@Param("bookNameKeyword") String bookNameKeyword, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.book b WHERE o.orderDate BETWEEN :startDate AND :endDate AND LOWER(b.title) LIKE LOWER(CONCAT('%', :bookNameKeyword, '%'))")
    Page<Order> findAllByOrderDateBetweenAndBookNameContainingIgnoreCase(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate, 
            @Param("bookNameKeyword") String bookNameKeyword, 
            Pageable pageable);

    // Admin: filter by userId and bookName
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.book b WHERE o.userId = :userId AND LOWER(b.title) LIKE LOWER(CONCAT('%', :bookNameKeyword, '%'))")
    Page<Order> findByUserIdAndBookNameContainingIgnoreCaseForAdmin(
        @Param("userId") Long userId, 
        @Param("bookNameKeyword") String bookNameKeyword, 
        Pageable pageable);

    // Admin: filter by userId, date range, and bookName
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.book b WHERE o.userId = :userId AND o.orderDate BETWEEN :startDate AND :endDate AND LOWER(b.title) LIKE LOWER(CONCAT('%', :bookNameKeyword, '%'))")
    Page<Order> findByUserIdAndOrderDateBetweenAndBookNameContainingIgnoreCaseForAdmin(
            @Param("userId") Long userId, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate, 
            @Param("bookNameKeyword") String bookNameKeyword, 
            Pageable pageable);

    // Query for User Consumption Statistics
    @Query("SELECT new com.bookstore.online_bookstore_backend.payload.response.UserConsumptionStatsDto(o.userId, 'TEMP_USERNAME', COUNT(o.id), SUM(o.totalPrice)) " +
           "FROM Order o " +
           "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY o.userId " +
           "ORDER BY SUM(o.totalPrice) DESC")
    List<UserConsumptionStatsDto> findUserConsumptionStatsBetweenDates(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);

} 