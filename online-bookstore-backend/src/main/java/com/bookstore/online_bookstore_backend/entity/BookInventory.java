package com.bookstore.online_bookstore_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图书库存实体类
 * 从Book表中分离出来，专门管理库存信息
 * 适合使用Redis进行高频访问和原子操作
 */
@Entity
@Table(name = "book_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookInventory {
    
    @Id
    private Long bookId; // 关联Book的ID，作为主键
    
    @Column(nullable = false)
    private Integer stock = 0; // 库存数量
    
    @Version // 乐观锁，防止并发更新冲突
    private Long version;
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 增加库存
     */
    public void addStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("增加的库存数量不能为负数");
        }
        this.stock += quantity;
    }
    
    /**
     * 减少库存
     */
    public boolean reduceStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("减少的库存数量不能为负数");
        }
        if (this.stock < quantity) {
            return false; // 库存不足
        }
        this.stock -= quantity;
        return true;
    }
}

