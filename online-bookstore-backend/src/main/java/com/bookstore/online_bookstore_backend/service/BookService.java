package com.bookstore.online_bookstore_backend.service; // 确保是正确的包名

import com.bookstore.online_bookstore_backend.dao.BookHybridDao; // 使用混合DAO
import com.bookstore.online_bookstore_backend.entity.Book;
import com.bookstore.online_bookstore_backend.repository.BookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 用于事务管理

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service // 标记这是一个 Spring Service Bean
public class BookService {

    private final BookHybridDao bookHybridDao; // 使用混合DAO
    private final BookRepository bookRepository; // 用于标签搜索
    private final ObjectMapper objectMapper; // JSON序列化工具
    
    @Autowired
    private TagService tagService; // 用于查询Neo4j标签关系

    @Autowired // Spring 自动注入依赖
    public BookService(BookHybridDao bookHybridDao, BookRepository bookRepository, ObjectMapper objectMapper) {
        this.bookHybridDao = bookHybridDao;
        this.bookRepository = bookRepository;
        this.objectMapper = objectMapper;
    }

    // 获取所有书籍 (支持分页) - 自动填充MongoDB数据
    @Transactional(readOnly = true) // 只读事务，可以优化性能
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookHybridDao.findAllWithMongoData(pageable); // 使用混合查询
    }

    // 根据分类获取书籍 (支持分页) - 自动填充MongoDB数据
    @Transactional(readOnly = true)
    public Page<Book> getBooksByCategory(String category, Pageable pageable) {
        if (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("all")) {
            return bookHybridDao.findAllWithMongoData(pageable); // 使用混合查询
        }
        return bookHybridDao.findByCategoryWithMongoData(category, pageable); // 使用混合查询
    }

    // 根据ID获取单本书籍详情 - 自动填充MongoDB数据
    @Transactional(readOnly = true)
    public Optional<Book> getBookById(Long id) {
        Book book = bookHybridDao.findByIdWithMongoData(id); // 使用混合查询
        return Optional.ofNullable(book);
    }

    // 添加新书 (或更新已有书籍，如果ID存在) - 同时保存到MySQL和MongoDB
    @Transactional // 读写事务
    public Book saveBook(Book book) {
        // 提取description（将存储在MongoDB中），cover现在存储在MySQL中
        String description = book.getDescription();
        
        // 在保存之前验证ISBN是否重复
        if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
            if (book.getId() == null) {
                // 新书籍，检查ISBN是否已存在
                if (bookHybridDao.existsByIsbnAndNotDeleted(book.getIsbn())) {
                    throw new RuntimeException("ISBN已存在: " + book.getIsbn());
                }
            } else {
                // 更新书籍，检查ISBN是否被其他书籍使用
                if (bookHybridDao.existsByIsbnAndIdNotAndNotDeleted(book.getIsbn(), book.getId())) {
                    throw new RuntimeException("ISBN已被其他书籍使用: " + book.getIsbn());
                }
            }
        }
        
        // 使用混合保存：同时保存MySQL和MongoDB数据
        return bookHybridDao.saveHybrid(book, description);
    }

    // 根据ID删除书籍 - 改为软删除（不删除MongoDB数据，保留历史）
    @Transactional
    public void deleteBookById(Long id) {
        bookHybridDao.softDeleteById(id); // 使用软删除而不是物理删除
        // 注意：不删除MongoDB数据，保留历史记录
    }

    // 物理删除（仅在需要时使用，比如管理员彻底清理）
    @Transactional
    public void hardDeleteBookById(Long id) {
        bookHybridDao.deleteById(id); // 物理删除MySQL数据
        bookHybridDao.deleteMongoDataByBookId(id); // 同时删除MongoDB数据
    }

    // 恢复已删除的书籍
    @Transactional
    public void restoreBookById(Long id) {
        bookHybridDao.restoreById(id);
    }

    // 按标题搜索书籍 - 自动填充MongoDB数据
    @Transactional(readOnly = true)
    public Page<Book> searchBooksByTitle(String keyword, Pageable pageable) {
        return bookHybridDao.findByTitleContainingIgnoreCaseWithMongoData(keyword, pageable); // 使用混合查询
    }

    // 管理员专用方法：获取所有书籍（包括已删除的）
    @Transactional(readOnly = true)
    public Page<Book> getAllBooksIncludingDeleted(Pageable pageable) {
        return bookHybridDao.findAllIncludingDeletedWithMongoData(pageable);
    }

    // 管理员专用方法：按分类获取所有书籍（包括已删除的）
    @Transactional(readOnly = true)
    public Page<Book> getBooksByCategoryIncludingDeleted(String category, Pageable pageable) {
        if (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("all")) {
            return bookHybridDao.findAllIncludingDeletedWithMongoData(pageable);
        }
        return bookHybridDao.findByCategoryIncludingDeletedWithMongoData(category, pageable);
    }

    // 管理员专用方法：按标题搜索所有书籍（包括已删除的）
    @Transactional(readOnly = true)
    public Page<Book> searchBooksByTitleIncludingDeleted(String keyword, Pageable pageable) {
        return bookHybridDao.findByTitleContainingIgnoreCaseIncludingDeletedWithMongoData(keyword, pageable);
    }

    // 管理员专用方法：获取已删除的书籍
    @Transactional(readOnly = true)
    public Page<Book> getDeletedBooks(Pageable pageable) {
        return bookHybridDao.findDeletedBooksWithMongoData(pageable);
    }

    /**
     * 按标签搜索图书（核心功能）
     * 1. 从Neo4j中查找与指定标签通过2次边连接相关的所有标签
     * 2. 在MySQL中搜索包含这些标签的图书
     * 
     * @param tagNames 用户选择的标签列表
     * @param pageable 分页参数
     * @return 符合条件的图书分页结果
     */
    @Transactional(readOnly = true)
    public Page<Book> searchBooksByTags(List<String> tagNames, Pageable pageable) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Page.empty(pageable);
        }

        // 步骤1：从Neo4j获取相关标签（包括2次边连接内的所有标签）
        Set<String> relatedTags = tagService.findRelatedTagNames(tagNames);
        
        // 如果没有找到相关标签，返回空结果
        if (relatedTags.isEmpty()) {
            return Page.empty(pageable);
        }

        // 步骤2：在MySQL中搜索包含这些标签的图书
        // 将Set转换为List再转换为JSON字符串（用于JSON_OVERLAPS函数）
        List<String> tagList = new ArrayList<>(relatedTags);
        try {
            String tagNamesJson = objectMapper.writeValueAsString(tagList);
            Page<Book> booksPage = bookRepository.findByTagsIn(tagNamesJson, pageable);
            
            // ⚠️ 重要：填充MongoDB数据（description字段）
            return bookHybridDao.fillMongoDataForPage(booksPage);
        } catch (JsonProcessingException e) {
            // JSON序列化失败，记录错误并返回空结果
            System.err.println("标签JSON序列化失败: " + e.getMessage());
            return Page.empty(pageable);
        }
    }
}