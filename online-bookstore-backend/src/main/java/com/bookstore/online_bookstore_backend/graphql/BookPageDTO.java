package com.bookstore.online_bookstore_backend.graphql;

import com.bookstore.online_bookstore_backend.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * GraphQL 分页结果 DTO
 * 用于封装 Spring Data Page 对象，返回给 GraphQL 客户端
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookPageDTO {
    
    /**
     * 当前页的书籍列表
     */
    private List<Book> content;
    
    /**
     * 总记录数
     */
    private long totalElements;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 当前页码（从0开始）
     */
    private int number;
    
    /**
     * 每页大小
     */
    private int size;
    
    /**
     * 当前页实际记录数
     */
    private int numberOfElements;
    
    /**
     * 是否是第一页
     */
    private boolean first;
    
    /**
     * 是否是最后一页
     */
    private boolean last;
    
    /**
     * 是否为空页
     */
    private boolean empty;
    
    /**
     * 从 Spring Data Page 对象构造 BookPageDTO
     * 
     * @param page Spring Data Page 对象
     */
    public BookPageDTO(Page<Book> page) {
        this.content = page.getContent();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.number = page.getNumber();
        this.size = page.getSize();
        this.numberOfElements = page.getNumberOfElements();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.empty = page.isEmpty();
    }
}


