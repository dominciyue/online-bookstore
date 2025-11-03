package com.bookstore.online_bookstore_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 作者查询响应DTO
 * 与author-service的AuthorResponse保持一致
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponseDTO {
    
    /**
     * 查询的书名
     */
    private String bookTitle;
    
    /**
     * 作者名
     */
    private String author;
    
    /**
     * 书籍ID
     */
    private Long bookId;
    
    /**
     * ISBN
     */
    private String isbn;
    
    /**
     * 出版社
     */
    private String publisher;
    
    /**
     * 该作者的其他书籍
     */
    private List<BookInfo> otherBooks;
    
    /**
     * 书籍信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookInfo {
        private Long id;
        private String title;
        private String isbn;
    }
}

