package com.bookstore.online_bookstore_backend.service;

import com.bookstore.online_bookstore_backend.dao.BookDao;
import com.bookstore.online_bookstore_backend.dao.CartItemDao;
import com.bookstore.online_bookstore_backend.entity.Book;
import com.bookstore.online_bookstore_backend.entity.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemDao cartItemDao;
    private final BookDao bookDao;
    
    @Autowired
    private BookInventoryService inventoryService; // 库存服务

    @Autowired
    public CartService(CartItemDao cartItemDao, BookDao bookDao) {
        this.cartItemDao = cartItemDao;
        this.bookDao = bookDao;
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByUserId(Long userId) {
        List<CartItem> items = cartItemDao.findByUserId(userId);
        // 为每个购物车项目填充瞬时字段 (title, price, cover)
        return items.stream().map(item -> {
            Optional<Book> bookOpt = bookDao.findById(item.getBookId());
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                item.setTitle(book.getTitle());
                item.setPrice(book.getPrice());
                item.setCover(book.getCover());
            }
            return item;
        }).collect(Collectors.toList());
    }

    @Transactional
    public CartItem addBookToCart(Long userId, Long bookId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("数量必须为正数。");
        }

        Book book = bookDao.findById(bookId)
                .orElseThrow(() -> new RuntimeException("未找到ID为: " + bookId + " 的书籍"));

        // 检查库存
        Integer currentStock = inventoryService.getStock(bookId);
        if (currentStock < quantity) {
            throw new RuntimeException("书籍库存不足: " + book.getTitle());
        }

        Optional<CartItem> existingItemOpt = cartItemDao.findByUserIdAndBookId(userId, bookId);

        CartItem cartItem;
        if (existingItemOpt.isPresent()) {
            cartItem = existingItemOpt.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            if (currentStock < newQuantity) { // 再次检查累计数量的库存
                 throw new RuntimeException("书籍库存不足: " + book.getTitle() + " (总计请求: " + newQuantity + ")");
            }
            cartItem.setQuantity(newQuantity);
        } else {
            cartItem = new CartItem(userId, bookId, quantity);
        }
        CartItem savedItem = cartItemDao.save(cartItem);
        // 为响应填充瞬时字段
        savedItem.setTitle(book.getTitle());
        savedItem.setPrice(book.getPrice());
        savedItem.setCover(book.getCover());
        return savedItem;
    }

    @Transactional
    public CartItem updateCartItemQuantity(Long userId, Long bookId, int quantity) {
        if (quantity <= 0) {
            // 如果数量小于等于0，则移除该商品项
            removeBookFromCart(userId, bookId);
            return null; // 或者抛出异常，或返回特定状态
        }

        CartItem cartItem = cartItemDao.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new RuntimeException("未找到用户 " + userId + " 的书籍 " + bookId + " 的购物车项"));

        Book book = bookDao.findById(bookId)
                .orElseThrow(() -> new RuntimeException("未找到ID为: " + bookId + " 的书籍"));

        // 检查库存
        Integer currentStock = inventoryService.getStock(bookId);
        if (currentStock < quantity) {
            throw new RuntimeException("书籍库存不足: " + book.getTitle());
        }

        cartItem.setQuantity(quantity);
        CartItem updatedItem = cartItemDao.save(cartItem);
        // 为响应填充瞬时字段
        updatedItem.setTitle(book.getTitle());
        updatedItem.setPrice(book.getPrice());
        updatedItem.setCover(book.getCover());
        return updatedItem;
    }

    @Transactional
    public void removeBookFromCart(Long userId, Long bookId) {
        CartItem cartItem = cartItemDao.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new RuntimeException("未找到用户 " + userId + " 的书籍 " + bookId + " 的购物车项。无法删除。"));
        cartItemDao.deleteById(cartItem.getId()); // 使用 CartItem 自己的 ID 进行删除
    }
    
    @Transactional
    public void clearCart(Long userId) {
        List<CartItem> userCartItems = cartItemDao.findByUserId(userId);
        if (userCartItems.isEmpty()) {
            return; // 如果购物车已空，则不执行任何操作
        }
        cartItemDao.deleteByUserId(userId);
    }
} 