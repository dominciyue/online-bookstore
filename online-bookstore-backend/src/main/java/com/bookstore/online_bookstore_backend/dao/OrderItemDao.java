package com.bookstore.online_bookstore_backend.dao;

import com.bookstore.online_bookstore_backend.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderItemDao {
    OrderItem save(OrderItem orderItem);
    List<OrderItem> saveAll(List<OrderItem> orderItems);
    Optional<OrderItem> findById(Long id);
    List<OrderItem> findByOrderId(Long orderId);
    Page<OrderItem> findByOrderId(Long orderId, Pageable pageable);
    void deleteById(Long id);
    void deleteByOrderId(Long orderId);
}
