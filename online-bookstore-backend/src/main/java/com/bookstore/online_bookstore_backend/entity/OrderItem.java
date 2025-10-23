package com.bookstore.online_bookstore_backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items") // 确保表名与数据库中一致
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Book book;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;

    // 如果需要在OrderItem中方便地获取Book的详细信息（如标题）用于展示，可以添加瞬时字段并在Service层填充
    @Transient
    private String bookTitle;
    @Transient
    private String bookCover;

    // 构造函数，方便Service层创建对象
    public OrderItem(Order order, Book book, int quantity, BigDecimal priceAtPurchase) {
        this.order = order;
        this.book = book;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }
} 