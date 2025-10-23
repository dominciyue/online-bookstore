package com.bookstore.online_bookstore_backend.repository;

import com.bookstore.online_bookstore_backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId);
    void deleteByUserIdAndBookId(Long userId, Long bookId); // 用于删除特定用户的特定商品
    void deleteByUserId(Long userId); // 用于清空特定用户的所有购物车商品
} 