package com.bookstore.online_bookstore_backend.graphql;

import com.bookstore.online_bookstore_backend.entity.Book;
import com.bookstore.online_bookstore_backend.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Optional;

/**
 * GraphQL 查询解析器
 * 处理书籍相关的 GraphQL 查询请求
 * 复用现有的 BookService 业务逻辑
 */
@Controller
public class BookQueryResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(BookQueryResolver.class);
    
    private final BookService bookService;
    
    @Autowired
    public BookQueryResolver(BookService bookService) {
        this.bookService = bookService;
    }
    
    /**
     * GraphQL 查询：按书名搜索书籍
     * 
     * 复用现有的 BookService.searchBooksByTitle 方法
     * 支持分页查询
     * 
     * GraphQL 查询示例：
     * query {
     *   searchBooksByTitle(title: "Java", page: 0, size: 10) {
     *     content {
     *       id
     *       title
     *       author
     *       price
     *     }
     *     totalElements
     *     totalPages
     *   }
     * }
     * 
     * 使用变量的查询示例：
     * query SearchBooks($title: String!, $page: Int, $size: Int) {
     *   searchBooksByTitle(title: $title, page: $page, size: $size) {
     *     content {
     *       id
     *       title
     *       author
     *       price
     *     }
     *     totalElements
     *   }
     * }
     * 
     * @param title 书籍名称（必需），支持模糊匹配
     * @param page 页码，默认为0（第一页）
     * @param size 每页大小，默认为10
     * @return BookPageDTO 包含分页信息和书籍列表
     */
    @QueryMapping
    public BookPageDTO searchBooksByTitle(
            @Argument String title,
            @Argument(name = "page") Integer page,
            @Argument(name = "size") Integer size) {
        
        logger.info("GraphQL Query: searchBooksByTitle - title={}, page={}, size={}", title, page, size);
        
        // 设置默认值
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0 && size <= 100) ? size : 10; // 限制最大100条
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        
        // 复用现有的 Service 方法进行查询
        Page<Book> bookPage = bookService.searchBooksByTitle(title, pageable);
        
        logger.info("GraphQL Query Result: Found {} books, total pages: {}", 
                    bookPage.getTotalElements(), bookPage.getTotalPages());
        
        // 转换为 GraphQL 返回类型
        return new BookPageDTO(bookPage);
    }
    
    /**
     * GraphQL 查询：根据ID获取书籍详情
     * 
     * 复用现有的 BookService.getBookById 方法
     * 
     * GraphQL 查询示例：
     * query {
     *   getBookById(id: "1") {
     *     id
     *     title
     *     author
     *     price
     *     description
     *   }
     * }
     * 
     * @param id 书籍ID
     * @return Book 书籍详情，如果不存在则返回null
     */
    @QueryMapping
    public Book getBookById(@Argument String id) {
        logger.info("GraphQL Query: getBookById - id={}", id);
        
        try {
            Long bookId = Long.parseLong(id);
            Optional<Book> bookOptional = bookService.getBookById(bookId);
            
            if (bookOptional.isPresent()) {
                logger.info("GraphQL Query Result: Book found - {}", bookOptional.get().getTitle());
                return bookOptional.get();
            } else {
                logger.warn("GraphQL Query Result: Book not found - id={}", id);
                return null;
            }
        } catch (NumberFormatException e) {
            logger.error("GraphQL Query Error: Invalid book ID format - {}", id);
            return null;
        }
    }
}


