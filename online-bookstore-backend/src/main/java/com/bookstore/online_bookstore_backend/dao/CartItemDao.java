package com.bookstore.online_bookstore_backend.dao;

import com.bookstore.online_bookstore_backend.entity.CartItem;
import java.util.List;
import java.util.Optional;

public interface CartItemDao {
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId);
    CartItem save(CartItem cartItem);
    void deleteById(Long id); // Corresponds to JpaRepository.deleteById(), used in CartService
    void deleteByUserId(Long userId);
    // Optional: void deleteByUserIdAndBookId(Long userId, Long bookId); // If direct usage is preferred
} 