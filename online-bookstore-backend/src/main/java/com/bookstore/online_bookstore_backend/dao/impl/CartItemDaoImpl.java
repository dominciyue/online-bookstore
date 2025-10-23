package com.bookstore.online_bookstore_backend.dao.impl;

import com.bookstore.online_bookstore_backend.dao.CartItemDao;
import com.bookstore.online_bookstore_backend.entity.CartItem;
import com.bookstore.online_bookstore_backend.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CartItemDaoImpl implements CartItemDao {

    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartItemDaoImpl(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public List<CartItem> findByUserId(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Override
    public Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId) {
        return cartItemRepository.findByUserIdAndBookId(userId, bookId);
    }

    @Override
    public CartItem save(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @Override
    public void deleteById(Long id) {
        cartItemRepository.deleteById(id);
    }

    @Override
    public void deleteByUserId(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    // Optional implementation if deleteByUserIdAndBookId from interface is uncommented
    // @Override
    // public void deleteByUserIdAndBookId(Long userId, Long bookId) {
    //     cartItemRepository.deleteByUserIdAndBookId(userId, bookId);
    // }
} 