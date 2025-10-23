package com.bookstore.online_bookstore_backend.dao.impl;

import com.bookstore.online_bookstore_backend.dao.OrderItemDao;
import com.bookstore.online_bookstore_backend.entity.OrderItem;
import com.bookstore.online_bookstore_backend.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderItemDaoImpl implements OrderItemDao {

    private final OrderItemRepository orderItemRepository;

    @Autowired
    public OrderItemDaoImpl(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public OrderItem save(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    @Override
    public List<OrderItem> saveAll(List<OrderItem> orderItems) {
        return orderItemRepository.saveAll(orderItems);
    }

    @Override
    public Optional<OrderItem> findById(Long id) {
        return orderItemRepository.findById(id);
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Override
    public Page<OrderItem> findByOrderId(Long orderId, Pageable pageable) {
        return orderItemRepository.findByOrderId(orderId, pageable);
    }

    @Override
    public void deleteById(Long id) {
        orderItemRepository.deleteById(id);
    }

    @Override
    public void deleteByOrderId(Long orderId) {
        orderItemRepository.deleteByOrderId(orderId);
    }
}
