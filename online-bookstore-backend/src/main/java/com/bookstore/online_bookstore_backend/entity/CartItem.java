package com.bookstore.online_bookstore_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 关联用户ID

    @Column(name = "book_id", nullable = false)
    private Long bookId; // 关联书籍ID

    @Column(nullable = false)
    private int quantity; // 商品数量

    // 瞬时字段，不存储在数据库，但方便传递给前端
    @Transient
    private String title; // 书籍标题

    @Transient
    private BigDecimal price; // 书籍价格

    @Transient
    private String cover; // 书籍封面

    // 给JPA和Service层使用的构造函数
    public CartItem(Long userId, Long bookId, int quantity) {
        this.userId = userId;
        this.bookId = bookId;
        this.quantity = quantity;
    }
} 