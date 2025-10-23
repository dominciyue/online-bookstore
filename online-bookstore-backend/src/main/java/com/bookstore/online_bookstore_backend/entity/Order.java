package com.bookstore.online_bookstore_backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders") // 确保表名与数据库中一致
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"orderItems"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false, length = 50)
    private String status = "PENDING"; // 默认状态

    @Column(name = "order_date") // , columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime orderDate;

    @Lob // 对于可能较长的文本
    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "created_at", updatable = false) //, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "updated_at") //, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.orderDate == null) {
            this.orderDate = now;
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        // totalPrice 应该由 OrderService 设置，此处不再设置默认值
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 