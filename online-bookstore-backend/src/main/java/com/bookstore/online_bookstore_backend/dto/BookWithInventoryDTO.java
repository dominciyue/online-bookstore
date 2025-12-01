package com.bookstore.online_bookstore_backend.dto;

import com.bookstore.online_bookstore_backend.entity.Book;
import com.bookstore.online_bookstore_backend.entity.BookInventory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 图书详情 DTO（包含库存信息）
 * 整合 Book 和 BookInventory 数据，提供给前端使用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookWithInventoryDTO {
    
    // Book 基础信息
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private BigDecimal price;
    private String cover;
    private String description;
    private String category;
    
    // 软删除相关
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    // 库存信息（来自 BookInventory）
    private Integer stock;
    
    /**
     * 从 Book 实体创建 DTO（不包含库存）
     */
    public static BookWithInventoryDTO fromBook(Book book) {
        if (book == null) {
            return null;
        }
        
        BookWithInventoryDTO dto = new BookWithInventoryDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setIsbn(book.getIsbn());
        dto.setPublisher(book.getPublisher());
        dto.setPrice(book.getPrice());
        dto.setCover(book.getCover());
        dto.setDescription(book.getDescription());
        dto.setCategory(book.getCategory());
        dto.setDeleted(book.getDeleted());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());
        dto.setDeletedAt(book.getDeletedAt());
        dto.setStock(0); // 默认库存为0
        
        return dto;
    }
    
    /**
     * 从 Book 和 BookInventory 创建 DTO
     */
    public static BookWithInventoryDTO fromBookAndInventory(Book book, BookInventory inventory) {
        BookWithInventoryDTO dto = fromBook(book);
        if (dto != null && inventory != null) {
            dto.setStock(inventory.getStock());
        }
        return dto;
    }
    
    /**
     * 从 Book 和库存数量创建 DTO
     */
    public static BookWithInventoryDTO fromBookAndStock(Book book, Integer stock) {
        BookWithInventoryDTO dto = fromBook(book);
        if (dto != null) {
            dto.setStock(stock != null ? stock : 0);
        }
        return dto;
    }
}

