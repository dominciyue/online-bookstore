package com.bookstore.online_bookstore_backend.service; // 确保是正确的包名

import com.bookstore.online_bookstore_backend.dao.BookDao; // Import BookDao
import com.bookstore.online_bookstore_backend.dto.BookWithInventoryDTO;
import com.bookstore.online_bookstore_backend.entity.Book;
// import com.bookstore.online_bookstore_backend.repository.BookRepository; // No longer directly used
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 用于事务管理

import java.util.Optional;

@Service // 标记这是一个 Spring Service Bean
public class BookService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookDao bookDao; // Use BookDao
    
    @Autowired
    private BookInventoryService inventoryService;

    @Autowired // Spring 自动注入 BookDao 的实例
    public BookService(BookDao bookDao) { // Inject BookDao
        this.bookDao = bookDao;
    }

    // 获取所有书籍 (支持分页)
    @Transactional(readOnly = true) // 只读事务，可以优化性能
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookDao.findAll(pageable); // Use bookDao
    }

    // 根据分类获取书籍 (支持分页)
    @Transactional(readOnly = true)
    public Page<Book> getBooksByCategory(String category, Pageable pageable) {
        if (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("all")) {
            return bookDao.findAll(pageable); // Use bookDao
        }
        return bookDao.findByCategory(category, pageable); // Use bookDao
    }

    // 根据ID获取单本书籍详情
    @Transactional(readOnly = true)
    public Optional<Book> getBookById(Long id) {
        // findById 返回一个 Optional<Book>，表示可能找到也可能找不到
        return bookDao.findById(id); // Use bookDao
    }
    
    // 根据ID获取书籍详情（包含库存信息）
    @Transactional(readOnly = true)
    public Optional<BookWithInventoryDTO> getBookWithInventoryById(Long id) {
        Optional<Book> bookOpt = bookDao.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            Integer stock = inventoryService.getStock(id);
            return Optional.of(BookWithInventoryDTO.fromBookAndStock(book, stock));
        }
        return Optional.empty();
    }

    // 添加新书 (或更新已有书籍，如果ID存在)
    @Transactional // 读写事务
    public Book saveBook(Book book) {
        // 在保存之前验证ISBN是否重复
        if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
            if (book.getId() == null) {
                // 新书籍，检查ISBN是否已存在
                if (bookDao.existsByIsbnAndNotDeleted(book.getIsbn())) {
                    throw new RuntimeException("ISBN已存在: " + book.getIsbn());
                }
            } else {
                // 更新书籍，检查ISBN是否被其他书籍使用
                if (bookDao.existsByIsbnAndIdNotAndNotDeleted(book.getIsbn(), book.getId())) {
                    throw new RuntimeException("ISBN已被其他书籍使用: " + book.getIsbn());
                }
            }
        }
        // save 方法可以用于新增 (如果book没有id或id不存在) 或更新 (如果book有id且存在)
        return bookDao.save(book); // Use bookDao
    }

    // 根据ID删除书籍 - 改为软删除
    @Transactional
    public void deleteBookById(Long id) {
        bookDao.softDeleteById(id); // 使用软删除而不是物理删除
    }

    // 物理删除（仅在需要时使用，比如管理员彻底清理）
    @Transactional
    public void hardDeleteBookById(Long id) {
        bookDao.deleteById(id); // 物理删除
    }

    // 恢复已删除的书籍
    @Transactional
    public void restoreBookById(Long id) {
        bookDao.restoreById(id);
    }

    // 您可以根据需要添加更多业务方法，例如按标题搜索等
    @Transactional(readOnly = true)
    public Page<Book> searchBooksByTitle(String keyword, Pageable pageable) {
        return bookDao.findByTitleContainingIgnoreCase(keyword, pageable); // Use bookDao
    }

    // 管理员专用方法：获取所有书籍（包括已删除的）
    @Transactional(readOnly = true)
    public Page<Book> getAllBooksIncludingDeleted(Pageable pageable) {
        return bookDao.findAllIncludingDeleted(pageable);
    }

    // 管理员专用方法：按分类获取所有书籍（包括已删除的）
    @Transactional(readOnly = true)
    public Page<Book> getBooksByCategoryIncludingDeleted(String category, Pageable pageable) {
        if (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("all")) {
            return bookDao.findAllIncludingDeleted(pageable);
        }
        return bookDao.findByCategoryIncludingDeleted(category, pageable);
    }

    // 管理员专用方法：按标题搜索所有书籍（包括已删除的）
    @Transactional(readOnly = true)
    public Page<Book> searchBooksByTitleIncludingDeleted(String keyword, Pageable pageable) {
        return bookDao.findByTitleContainingIgnoreCaseIncludingDeleted(keyword, pageable);
    }

    // 管理员专用方法：获取已删除的书籍
    @Transactional(readOnly = true)
    public Page<Book> getDeletedBooks(Pageable pageable) {
        return bookDao.findDeletedBooks(pageable);
    }
}