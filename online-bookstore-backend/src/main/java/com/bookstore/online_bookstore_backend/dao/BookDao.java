package com.bookstore.online_bookstore_backend.dao;

import com.bookstore.online_bookstore_backend.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BookDao {
    Page<Book> findAll(Pageable pageable);
    Page<Book> findByCategory(String category, Pageable pageable);
    Optional<Book> findById(Long id);
    Book save(Book book);
    void deleteById(Long id);
    Page<Book> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    
    // 软删除相关方法
    void softDeleteById(Long id);
    void restoreById(Long id);
    
    // 管理员专用方法（包含已删除的书籍）
    Page<Book> findAllIncludingDeleted(Pageable pageable);
    Page<Book> findByCategoryIncludingDeleted(String category, Pageable pageable);
    Page<Book> findByTitleContainingIgnoreCaseIncludingDeleted(String keyword, Pageable pageable);
    Page<Book> findDeletedBooks(Pageable pageable);
    
    // ISBN验证方法
    boolean existsByIsbnAndNotDeleted(String isbn);
    boolean existsByIsbnAndIdNotAndNotDeleted(String isbn, Long id);
} 