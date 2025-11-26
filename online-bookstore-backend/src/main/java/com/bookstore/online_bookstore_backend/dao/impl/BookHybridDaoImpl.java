package com.bookstore.online_bookstore_backend.dao.impl;

import com.bookstore.online_bookstore_backend.dao.BookHybridDao;
import com.bookstore.online_bookstore_backend.entity.Book;
import com.bookstore.online_bookstore_backend.entity.mongo.BookMongoDocument;
import com.bookstore.online_bookstore_backend.repository.BookRepository;
import com.bookstore.online_bookstore_backend.repository.mongo.BookMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 混合DAO实现 - 整合MySQL和MongoDB操作
 */
@Repository
@Primary  // 标记为主要实现，当有多个BookDao实现时优先使用此实现
public class BookHybridDaoImpl extends BookDaoImpl implements BookHybridDao {

    private static final Logger logger = LoggerFactory.getLogger(BookHybridDaoImpl.class);
    
    private final BookMongoRepository bookMongoRepository;

    @Autowired
    public BookHybridDaoImpl(BookRepository bookRepository, 
                             BookMongoRepository bookMongoRepository) {
        super(bookRepository);
        this.bookMongoRepository = bookMongoRepository;
        logger.info("✓ BookHybridDaoImpl initialized with MongoDB support");
    }

    @Override
    public Optional<BookMongoDocument> findMongoDataByBookId(Long bookId) {
        return bookMongoRepository.findByBookId(bookId);
    }

    @Override
    public BookMongoDocument saveMongoData(BookMongoDocument mongoDoc) {
        mongoDoc.setUpdatedAt(LocalDateTime.now());
        if (mongoDoc.getCreatedAt() == null) {
            mongoDoc.setCreatedAt(LocalDateTime.now());
        }
        return bookMongoRepository.save(mongoDoc);
    }

    @Override
    public Book saveHybrid(Book book, String description) {
        // 1. 保存MySQL数据（包含cover）
        Book savedBook = this.save(book);
        
        // 2. 保存或更新MongoDB数据（只存储description）
        Optional<BookMongoDocument> existingMongo = bookMongoRepository.findByBookId(savedBook.getId());
        BookMongoDocument mongoDoc;
        
        if (existingMongo.isPresent()) {
            // 更新现有文档
            mongoDoc = existingMongo.get();
        } else {
            // 创建新文档
            mongoDoc = new BookMongoDocument();
            mongoDoc.setBookId(savedBook.getId());
            mongoDoc.setCreatedAt(LocalDateTime.now());
        }
        
        // 只更新description字段
        mongoDoc.setDescription(description);
        mongoDoc.setUpdatedAt(LocalDateTime.now());
        
        bookMongoRepository.save(mongoDoc);
        
        // 3. 填充Book的瞬时字段（description从MongoDB加载）
        savedBook.setDescription(description);
        
        return savedBook;
    }

    @Override
    public Book findByIdWithMongoData(Long id) {
        Optional<Book> bookOpt = this.findById(id);
        if (bookOpt.isEmpty()) {
            return null;
        }
        
        Book book = bookOpt.get();
        enrichBookWithMongoData(book);
        return book;
    }

    @Override
    public Page<Book> findAllWithMongoData(Pageable pageable) {
        Page<Book> booksPage = this.findAll(pageable);
        return enrichPageWithMongoData(booksPage);
    }

    @Override
    public Page<Book> findByCategoryWithMongoData(String category, Pageable pageable) {
        Page<Book> booksPage = this.findByCategory(category, pageable);
        return enrichPageWithMongoData(booksPage);
    }

    @Override
    public Page<Book> findByTitleContainingIgnoreCaseWithMongoData(String keyword, Pageable pageable) {
        Page<Book> booksPage = this.findByTitleContainingIgnoreCase(keyword, pageable);
        return enrichPageWithMongoData(booksPage);
    }

    @Override
    public Page<Book> findAllIncludingDeletedWithMongoData(Pageable pageable) {
        Page<Book> booksPage = this.findAllIncludingDeleted(pageable);
        return enrichPageWithMongoData(booksPage);
    }

    @Override
    public Page<Book> findByCategoryIncludingDeletedWithMongoData(String category, Pageable pageable) {
        Page<Book> booksPage = this.findByCategoryIncludingDeleted(category, pageable);
        return enrichPageWithMongoData(booksPage);
    }

    @Override
    public Page<Book> findByTitleContainingIgnoreCaseIncludingDeletedWithMongoData(String keyword, Pageable pageable) {
        Page<Book> booksPage = this.findByTitleContainingIgnoreCaseIncludingDeleted(keyword, pageable);
        return enrichPageWithMongoData(booksPage);
    }

    @Override
    public Page<Book> findDeletedBooksWithMongoData(Pageable pageable) {
        Page<Book> booksPage = this.findDeletedBooks(pageable);
        return enrichPageWithMongoData(booksPage);
    }

    @Override
    public void deleteMongoDataByBookId(Long bookId) {
        bookMongoRepository.deleteByBookId(bookId);
    }

    /**
     * 从MongoDB加载数据填充Book的瞬时字段
     * @param book Book实体
     */
    private void enrichBookWithMongoData(Book book) {
        if (book == null || book.getId() == null) {
            logger.debug("Book is null or has no ID, skipping MongoDB enrichment");
            return;
        }
        
        logger.debug("Enriching Book ID: {} from MongoDB", book.getId());
        Optional<BookMongoDocument> mongoDoc = bookMongoRepository.findByBookId(book.getId());
        
        if (mongoDoc.isPresent()) {
            BookMongoDocument doc = mongoDoc.get();
            String description = doc.getDescription();
            book.setDescription(description);
            logger.debug("✓ Book ID: {} enriched with description: {}", book.getId(), 
                        description != null ? description.substring(0, Math.min(50, description.length())) + "..." : "null");
        } else {
            logger.warn("⚠ No MongoDB document found for Book ID: {}", book.getId());
        }
    }

    /**
     * 为分页结果填充MongoDB数据
     * @param booksPage 分页结果
     * @return 填充后的分页结果
     */
    private Page<Book> enrichPageWithMongoData(Page<Book> booksPage) {
        List<Book> enrichedBooks = booksPage.getContent().stream()
            .peek(this::enrichBookWithMongoData)
            .collect(Collectors.toList());
        
        return new PageImpl<>(enrichedBooks, booksPage.getPageable(), booksPage.getTotalElements());
    }
}

