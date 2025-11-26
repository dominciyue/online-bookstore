package com.bookstore.online_bookstore_backend.entity.mongo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB文档实体 - 存储书籍的富媒体内容和扩展信息
 * 对应MongoDB中的books集合
 */
@Document(collection = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookMongoDocument {
    
    @Id
    private String id; // MongoDB的_id，自动生成
    
    @Indexed(unique = true)
    @Field("bookId")
    private Long bookId; // 对应MySQL中的Book.id，用于关联
    
    @Field("cover")
    private String cover; // 封面图片URL或Base64
    
    @Field("description")
    private String description; // 书籍详细描述（大文本）
    
    @Field("reviews")
    private List<BookReview> reviews = new ArrayList<>(); // 书评列表
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    /**
     * 内嵌文档：书评
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookReview {
        private Long userId;
        private String username;
        private Integer rating; // 评分 1-5
        private String content; // 评论内容
        private LocalDateTime reviewDate;
    }
}

