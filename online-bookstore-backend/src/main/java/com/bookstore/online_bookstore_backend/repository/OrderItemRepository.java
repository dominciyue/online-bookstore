package com.bookstore.online_bookstore_backend.repository;

import com.bookstore.online_bookstore_backend.entity.OrderItem;
import com.bookstore.online_bookstore_backend.payload.response.BookSalesStatsDto;
import com.bookstore.online_bookstore_backend.payload.response.PersonalBookStatsItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

// import java.util.List; // 如果需要特定查询，例如按 bookId 查找订单项

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // 根据订单ID查找订单项
    List<OrderItem> findByOrderId(Long orderId);
    
    // 根据订单ID分页查找订单项
    Page<OrderItem> findByOrderId(Long orderId, Pageable pageable);
    
    // 根据订单ID删除订单项
    void deleteByOrderId(Long orderId);

    @Query("SELECT new com.bookstore.online_bookstore_backend.payload.response.BookSalesStatsDto(oi.book.id, oi.book.title, oi.book.author, oi.book.cover, SUM(oi.quantity), SUM(oi.priceAtPurchase * oi.quantity)) " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.book.id, oi.book.title, oi.book.author, oi.book.cover " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<BookSalesStatsDto> findBookSalesStatsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new com.bookstore.online_bookstore_backend.payload.response.PersonalBookStatsItemDto(oi.book.id, oi.book.title, oi.book.author, oi.book.cover, SUM(oi.quantity), SUM(oi.priceAtPurchase * oi.quantity)) " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.userId = :userId AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.book.id, oi.book.title, oi.book.author, oi.book.cover " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<PersonalBookStatsItemDto> findPersonalBookStatsByUserAndDateRange(
            @Param("userId") Long userId, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
} 