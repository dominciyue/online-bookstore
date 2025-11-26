package com.bookstore.online_bookstore_backend.service; // 确保是正确的包名

import com.bookstore.online_bookstore_backend.dao.BookHybridDao; // 使用混合DAO
import com.bookstore.online_bookstore_backend.entity.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 用于事务管理

import java.util.Optional;

@Service // 标记这是一个 Spring Service Bean
public class BookService {

    private final BookHybridDao bookHybridDao; // 使用混合DAO

    @Autowired // Spring 自动注入 BookHybridDao 的实例
    public BookService(BookHybridDao bookHybridDao) {
        this.bookHybridDao = bookHybridDao;
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
}