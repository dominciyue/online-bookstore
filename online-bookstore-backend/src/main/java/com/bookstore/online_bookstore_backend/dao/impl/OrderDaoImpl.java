package com.bookstore.online_bookstore_backend.dao.impl;

import com.bookstore.online_bookstore_backend.dao.OrderDao;
import com.bookstore.online_bookstore_backend.entity.Order;
import com.bookstore.online_bookstore_backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderDaoImpl implements OrderDao {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderDaoImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public List<Order> findByUserIdOrderByOrderDateDesc(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Page<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId, pageable);
    }

    @Override
    public Page<Order> findByUserIdAndOrderDateBetweenOrderByOrderDateDesc(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return orderRepository.findByUserIdAndOrderDateBetweenOrderByOrderDateDesc(userId, startDate, endDate, pageable);
    }

    @Override
    public Page<Order> findByUserIdAndBookNameContainingIgnoreCase(Long userId, String bookNameKeyword, Pageable pageable) {
        return orderRepository.findByUserIdAndBookNameContainingIgnoreCase(userId, bookNameKeyword, pageable);
    }

    @Override
    public Page<Order> findByUserIdAndOrderDateBetweenAndBookNameContainingIgnoreCase(Long userId, LocalDateTime startDate, LocalDateTime endDate, String bookNameKeyword, Pageable pageable) {
        return orderRepository.findByUserIdAndOrderDateBetweenAndBookNameContainingIgnoreCase(userId, startDate, endDate, bookNameKeyword, pageable);
    }

    @Override
    public Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable) {
        return orderRepository.findAllByOrderByOrderDateDesc(pageable);
    }

    @Override
    public Page<Order> findAllByOrderDateBetweenOrderByOrderDateDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return orderRepository.findAllByOrderDateBetweenOrderByOrderDateDesc(startDate, endDate, pageable);
    }

    @Override
    public Page<Order> findAllByBookNameContainingIgnoreCase(String bookNameKeyword, Pageable pageable) {
        return orderRepository.findAllByBookNameContainingIgnoreCase(bookNameKeyword, pageable);
    }

    @Override
    public Page<Order> findAllByOrderDateBetweenAndBookNameContainingIgnoreCase(LocalDateTime startDate, LocalDateTime endDate, String bookNameKeyword, Pageable pageable) {
        return orderRepository.findAllByOrderDateBetweenAndBookNameContainingIgnoreCase(startDate, endDate, bookNameKeyword, pageable);
    }

    @Override
    public Page<Order> findByUserIdAndBookNameContainingIgnoreCaseForAdmin(Long userId, String bookNameKeyword, Pageable pageable) {
        return orderRepository.findByUserIdAndBookNameContainingIgnoreCaseForAdmin(userId, bookNameKeyword, pageable);
    }

    @Override
    public Page<Order> findByUserIdAndOrderDateBetweenAndBookNameContainingIgnoreCaseForAdmin(Long userId, LocalDateTime startDate, LocalDateTime endDate, String bookNameKeyword, Pageable pageable) {
        return orderRepository.findByUserIdAndOrderDateBetweenAndBookNameContainingIgnoreCaseForAdmin(userId, startDate, endDate, bookNameKeyword, pageable);
    }
} 