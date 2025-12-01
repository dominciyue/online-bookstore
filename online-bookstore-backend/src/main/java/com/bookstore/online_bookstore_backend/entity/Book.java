package com.bookstore.online_bookstore_backend.entity; // 确保这是您正确的包名

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// 如果您在 pom.xml 中添加了 Lombok 依赖
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "books") // 指定数据库中的表名为 "books"
// Lombok 注解 (如果使用了 Lombok)
@Data // 自动生成 getter, setter, toString, equals, hashCode
@NoArgsConstructor // 自动生成无参构造函数
@AllArgsConstructor // 自动生成全参构造函数
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 通常用于MySQL的自增主键
    private Long id; // 主键，使用 Long 类型

    @Column(nullable = false, length = 255) // 不允许为空，最大长度255
    private String title;

    @Column(length = 100)
    private String author;

    @Column(name = "isbn", length = 20, unique = true) // ISBN字段，通常10或13位，可以允许稍长，并设为唯一
    private String isbn;

    @Column(name = "publisher", length = 100) // 出版社字段
    private String publisher;

    @Column(precision = 10, scale = 2) // 对于 BigDecimal, precision 和 scale 是合适的
    private BigDecimal price;

    @Column(length = 1000) // 封面图片的URL通常不会太长，但可以适当放宽
    private String cover;

    @Lob // 表示这是一个可能较大的文本字段
    @Column(columnDefinition = "TEXT") // 对于较长的描述，使用TEXT类型
    private String description;

    @Column(length = 50)
    private String category; // 书籍分类 (可以考虑之后将其设计为单独的Category实体并建立关联)

    // 注意：库存信息已迁移到 BookInventory 表，用于更好的缓存管理和并发控制
    // private Integer stock; // 库存数量 - 已废弃，请使用 BookInventory

    // 软删除标记字段
    @Column(nullable = false)
    private Boolean deleted = false; // 默认为未删除状态

    // 创建时间
    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    // 更新时间
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    // 删除时间（软删除时记录删除时间）
    @Column(name = "deleted_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.deleted == null) {
            this.deleted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 软删除方法
    public void markAsDeleted() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 恢复删除方法
    public void markAsActive() {
        this.deleted = false;
        this.deletedAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    // 如果没有使用 Lombok，您需要手动添加构造函数、getter 和 setter 方法：
    // public Book() {}

    // public Book(String title, String author, Double price, String cover, String description, String category, Integer stock) {
    //     this.title = title;
    //     this.author = author;
    //     this.price = price;
    //     this.cover = cover;
    //     this.description = description;
    //     this.category = category;
    //     this.stock = stock;
    // }

    // public Long getId() { return id; }
    // public void setId(Long id) { this.id = id; }
    // public String getTitle() { return title; }
    // public void setTitle(String title) { this.title = title; }
    // // ... 其他所有字段的 getter 和 setter
}