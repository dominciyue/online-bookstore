package com.bookstore.online_bookstore_backend.dao;

import com.bookstore.online_bookstore_backend.entity.Book;
import com.bookstore.online_bookstore_backend.entity.mongo.BookMongoDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 混合DAO接口 - 整合MySQL和MongoDB操作
 */
public interface BookHybridDao extends BookDao {
    
    /**
     * 根据bookId获取MongoDB数据
     * @param bookId 书籍ID
     * @return MongoDB文档
     */
    Optional<BookMongoDocument> findMongoDataByBookId(Long bookId);
    
    /**
     * 保存MongoDB数据
     * @param mongoDoc MongoDB文档
     * @return 保存后的文档
     */
    BookMongoDocument saveMongoData(BookMongoDocument mongoDoc);
    
    /**
     * 同时保存MySQL和MongoDB数据（只保存description到MongoDB，cover保留在MySQL）
     * @param book MySQL实体
     * @param description 描述
     * @return 保存后的Book实体（已填充MongoDB数据）
     */
    Book saveHybrid(Book book, String description);
    
    /**
     * 查询Book并自动填充MongoDB数据
     * @param id 书籍ID
     * @return 完整的Book实体
     */
    Book findByIdWithMongoData(Long id);
    
    /**
     * 分页查询Book并自动填充MongoDB数据
     * @param pageable 分页参数
     * @return 完整的Book实体分页
     */
    Page<Book> findAllWithMongoData(Pageable pageable);
    
    /**
     * 按分类分页查询并填充MongoDB数据
     * @param category 分类
     * @param pageable 分页参数
     * @return 完整的Book实体分页
     */
    Page<Book> findByCategoryWithMongoData(String category, Pageable pageable);
    
    /**
     * 按标题搜索并填充MongoDB数据
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 完整的Book实体分页
     */
    Page<Book> findByTitleContainingIgnoreCaseWithMongoData(String keyword, Pageable pageable);
    
    /**
     * 管理员方法：获取所有书籍（包括已删除）并填充MongoDB数据
     */
    Page<Book> findAllIncludingDeletedWithMongoData(Pageable pageable);
    
    /**
     * 管理员方法：按分类获取书籍（包括已删除）并填充MongoDB数据
     */
    Page<Book> findByCategoryIncludingDeletedWithMongoData(String category, Pageable pageable);
    
    /**
     * 管理员方法：按标题搜索书籍（包括已删除）并填充MongoDB数据
     */
    Page<Book> findByTitleContainingIgnoreCaseIncludingDeletedWithMongoData(String keyword, Pageable pageable);
    
    /**
     * 管理员方法：获取已删除的书籍并填充MongoDB数据
     */
    Page<Book> findDeletedBooksWithMongoData(Pageable pageable);
    
    /**
     * 删除MongoDB数据
     * @param bookId 书籍ID
     */
    void deleteMongoDataByBookId(Long bookId);
    
    /**
     * 为已有的Page<Book>填充MongoDB数据
     * 用于标签搜索等直接从Repository获取数据的场景
     * @param booksPage 从Repository获取的Book分页
     * @return 填充MongoDB数据后的Book分页
     */
    Page<Book> fillMongoDataForPage(Page<Book> booksPage);
}

