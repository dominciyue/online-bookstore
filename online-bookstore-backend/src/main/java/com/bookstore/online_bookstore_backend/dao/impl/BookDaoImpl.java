package com.bookstore.online_bookstore_backend.dao.impl;

import com.bookstore.online_bookstore_backend.dao.BookDao;
import com.bookstore.online_bookstore_backend.entity.Book;
import com.bookstore.online_bookstore_backend.repository.BookRepository;
import com.bookstore.online_bookstore_backend.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BookDaoImpl implements BookDao {
    
    private static final Logger logger = LoggerFactory.getLogger(BookDaoImpl.class);

    private final BookRepository bookRepository;
    
    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    public BookDaoImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Override
    public Page<Book> findByCategory(String category, Pageable pageable) {
        return bookRepository.findByCategory(category, pageable);
    }

    @Override
    public Optional<Book> findById(Long id) {
        // 1. Try Redis cache first
        Book cachedBook = redisCacheService.getCachedBook(id);
        if (cachedBook != null) {
            logger.info("✅ Book from Redis: ID={}, Title={}", id, cachedBook.getTitle());
            return Optional.of(cachedBook);
        }
        
        // 2. Cache miss, query database
        logger.info("⚠️ Redis miss, query DB: ID={}", id);
        Optional<Book> bookOpt = bookRepository.findById(id);
        
        // 3. Cache to Redis if found
        bookOpt.ifPresent(book -> {
            redisCacheService.cacheBook(book);
            logger.info("📦 Cached to Redis: ID={}, Title={}", book.getId(), book.getTitle());
        });
        
        return bookOpt;
    }

    @Override
    public Book save(Book book) {
        // 1. Save to database
        Book savedBook = bookRepository.save(book);
        
        // 2. Update Redis cache (Write-Through)
        redisCacheService.cacheBook(savedBook);
        logger.info("✅ Book saved and cached: ID={}, Title={}", savedBook.getId(), savedBook.getTitle());
        
        return savedBook;
    }

    @Override
    public void deleteById(Long id) {
        // Delete from database and evict cache
        bookRepository.deleteById(id);
        redisCacheService.evictBook(id);
        logger.info("✅ Book deleted: ID={}", id);
    }

    @Override
    public Page<Book> findByTitleContainingIgnoreCase(String keyword, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

    // 软删除相关方法实现
    @Override
    public void softDeleteById(Long id) {
        Optional<Book> bookOpt = bookRepository.findByIdIgnoreDeleted(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.markAsDeleted();
            bookRepository.save(book);
            redisCacheService.evictBook(id);
            logger.info("✅ Book soft-deleted: ID={}", id);
        } else {
            throw new RuntimeException("Book not found, ID: " + id);
        }
    }

    @Override
    public void restoreById(Long id) {
        Optional<Book> bookOpt = bookRepository.findByIdIgnoreDeleted(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.markAsActive();
            bookRepository.save(book);
            redisCacheService.cacheBook(book);
            logger.info("✅ Book restored: ID={}", id);
        } else {
            throw new RuntimeException("Book not found, ID: " + id);
        }
    }

    @Override
    public Page<Book> findAllIncludingDeleted(Pageable pageable) {
        return bookRepository.findAllIncludingDeleted(pageable);
    }

    @Override
    public Page<Book> findByCategoryIncludingDeleted(String category, Pageable pageable) {
        return bookRepository.findByCategoryIncludingDeleted(category, pageable);
    }

    @Override
    public Page<Book> findByTitleContainingIgnoreCaseIncludingDeleted(String keyword, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCaseIncludingDeleted(keyword, pageable);
    }

    @Override
    public Page<Book> findDeletedBooks(Pageable pageable) {
        return bookRepository.findDeletedBooks(pageable);
    }

    @Override
    public boolean existsByIsbnAndNotDeleted(String isbn) {
        return bookRepository.existsByIsbnAndNotDeleted(isbn);
    }

    @Override
    public boolean existsByIsbnAndIdNotAndNotDeleted(String isbn, Long id) {
        return bookRepository.existsByIsbnAndIdNotAndNotDeleted(isbn, id);
    }
}
