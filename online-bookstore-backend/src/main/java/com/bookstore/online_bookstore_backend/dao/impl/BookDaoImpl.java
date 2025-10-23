package com.bookstore.online_bookstore_backend.dao.impl;

import com.bookstore.online_bookstore_backend.dao.BookDao;
import com.bookstore.online_bookstore_backend.entity.Book;
import com.bookstore.online_bookstore_backend.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BookDaoImpl implements BookDao {

    private final BookRepository bookRepository;

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
        return bookRepository.findById(id);
    }

    @Override
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
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
        } else {
            throw new RuntimeException("书籍未找到，ID: " + id);
        }
    }

    @Override
    public void restoreById(Long id) {
        Optional<Book> bookOpt = bookRepository.findByIdIgnoreDeleted(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.markAsActive();
            bookRepository.save(book);
        } else {
            throw new RuntimeException("书籍未找到，ID: " + id);
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
