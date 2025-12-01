package com.bookstore.online_bookstore_backend.controller; // 确保是正确的包名

import com.bookstore.online_bookstore_backend.dto.BookWithInventoryDTO;
import com.bookstore.online_bookstore_backend.entity.Book;
import com.bookstore.online_bookstore_backend.service.BookInventoryService;
import com.bookstore.online_bookstore_backend.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*; // 导入所有 Web 注解

import java.util.Map;
import java.util.Optional;

@RestController // 组合了 @Controller 和 @ResponseBody，表示所有方法返回的数据直接写入HTTP响应体
@RequestMapping("/api/books") // 此 Controller 处理的所有请求都以 /api/books 为前缀
public class BookController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;
    
    @Autowired
    private BookInventoryService inventoryService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET /api/books - 获取所有书籍 (支持分页、分类、标题搜索)
    @GetMapping
    public ResponseEntity<Page<Book>> getBooks(
            @RequestParam(required = false) String category,
            @RequestParam(name = "title", required = false) String titleKeyword,
            @RequestParam(defaultValue = "0") int page, // 页码 (0-indexed)
            @RequestParam(defaultValue = "10") int size, // 每页大小
            @RequestParam(defaultValue = "id,asc") String[] sort) { // 排序字段和方向
        
        Sort.Direction direction = sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<Book> booksPage;
        if (titleKeyword != null && !titleKeyword.trim().isEmpty()) {
            booksPage = bookService.searchBooksByTitle(titleKeyword, pageable);
        } else if (category != null && !category.trim().isEmpty()) {
            booksPage = bookService.getBooksByCategory(category, pageable);
        } else {
            booksPage = bookService.getAllBooks(pageable);
        }
        return ResponseEntity.ok(booksPage); // 返回 200 OK 和书籍列表
    }

    // GET /api/books/{id} - 根据ID获取书籍详情（包含库存）
    @GetMapping("/{id}")
    public ResponseEntity<BookWithInventoryDTO> getBookById(@PathVariable Long id) {
        logger.info("Get book details: ID={}", id);
        Optional<BookWithInventoryDTO> bookOptional = bookService.getBookWithInventoryById(id);
        // 如果找到了书，返回 200 OK 和书对象；否则返回 404 Not Found
        return bookOptional.map(ResponseEntity::ok)
                           .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/books - 添加一本新书（包含库存信息）
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addBook(@RequestBody BookWithInventoryDTO bookDTO) {
        try {
            logger.info("Add new book: Title={}, Stock={}", bookDTO.getTitle(), bookDTO.getStock());
            
            // 1. 保存图书基础信息
            Book book = new Book();
            book.setTitle(bookDTO.getTitle());
            book.setAuthor(bookDTO.getAuthor());
            book.setIsbn(bookDTO.getIsbn());
            book.setPublisher(bookDTO.getPublisher());
            book.setPrice(bookDTO.getPrice());
            book.setCover(bookDTO.getCover());
            book.setDescription(bookDTO.getDescription());
            book.setCategory(bookDTO.getCategory());
            
            Book savedBook = bookService.saveBook(book);
            
            // 2. 保存库存信息
            if (bookDTO.getStock() != null && bookDTO.getStock() > 0) {
                inventoryService.updateInventory(savedBook.getId(), bookDTO.getStock());
            }
            
            // 3. 返回完整的DTO
            BookWithInventoryDTO resultDTO = BookWithInventoryDTO.fromBookAndStock(savedBook, bookDTO.getStock());
            logger.info("✅ New book added successfully: ID={}, Title={}", savedBook.getId(), savedBook.getTitle());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(resultDTO);
        } catch (RuntimeException e) {
            logger.error("❌ Failed to add new book: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // PUT /api/books/{id} - 更新已有书籍信息（包含库存）
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody BookWithInventoryDTO bookDetails) {
        try {
            logger.info("Update book: ID={}, Title={}, Stock={}", id, bookDetails.getTitle(), bookDetails.getStock());
            
            Optional<Book> existingBookOptional = bookService.getBookById(id);
            if (existingBookOptional.isPresent()) {
                Book existingBook = existingBookOptional.get();
                
                // 1. 更新图书基础信息
                existingBook.setTitle(bookDetails.getTitle());
                existingBook.setAuthor(bookDetails.getAuthor());
                existingBook.setIsbn(bookDetails.getIsbn());
                existingBook.setPublisher(bookDetails.getPublisher());
                existingBook.setPrice(bookDetails.getPrice());
                existingBook.setCover(bookDetails.getCover());
                existingBook.setDescription(bookDetails.getDescription());
                existingBook.setCategory(bookDetails.getCategory());
                
                Book updatedBook = bookService.saveBook(existingBook);
                
                // 2. 更新库存信息
                if (bookDetails.getStock() != null) {
                    inventoryService.updateInventory(id, bookDetails.getStock());
                }
                
                // 3. 返回完整的DTO
                BookWithInventoryDTO resultDTO = BookWithInventoryDTO.fromBookAndStock(updatedBook, bookDetails.getStock());
                logger.info("✅ Book updated successfully: ID={}, Title={}", id, updatedBook.getTitle());
                
                return ResponseEntity.ok(resultDTO);
            } else {
                logger.warn("⚠️ Book not found: ID={}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            logger.error("❌ Failed to update book: ID={}, Error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // DELETE /api/books/{id} - 根据ID软删除书籍
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBookById(id);
            return ResponseEntity.ok(Map.of("message", "Book soft-deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    // POST /api/books/{id}/restore - 恢复已删除的书籍
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> restoreBook(@PathVariable Long id) {
        try {
            bookService.restoreBookById(id);
            return ResponseEntity.ok(Map.of("message", "Book restored successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    // DELETE /api/books/{id}/hard - 物理删除书籍（管理员专用，同时删除库存）
    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> hardDeleteBook(@PathVariable Long id) {
        try {
            logger.info("Hard delete book: ID={}", id);
            
            Optional<Book> bookOptional = bookService.getBookById(id);
            if (bookOptional.isPresent()) {
                // 1. 删除库存信息
                inventoryService.deleteInventory(id);
                
                // 2. 删除图书信息
                bookService.hardDeleteBookById(id);
                
                logger.info("✅ Book permanently deleted: ID={}", id);
                return ResponseEntity.ok(Map.of("message", "Book permanently deleted"));
            } else {
                logger.warn("⚠️ Book not found: ID={}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("❌ Failed to delete book: ID={}, Error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to delete: " + e.getMessage()));
        }
    }
    
    // PUT /api/books/{id}/inventory - 更新图书库存
    @PutMapping("/{id}/inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateInventory(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        try {
            Integer stock = request.get("stock");
            if (stock == null || stock < 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid stock quantity"));
            }
            
            logger.info("Update inventory: BookID={}, NewStock={}", id, stock);
            inventoryService.updateInventory(id, stock);
            logger.info("✅ Inventory updated successfully: BookID={}, Stock={}", id, stock);
            
            return ResponseEntity.ok(Map.of("message", "Inventory updated successfully", "stock", stock));
        } catch (Exception e) {
            logger.error("❌ Failed to update inventory: BookID={}, Error={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to update inventory: " + e.getMessage()));
        }
    }
    
    // GET /api/books/{id}/inventory - 获取图书库存
    @GetMapping("/{id}/inventory")
    public ResponseEntity<?> getInventory(@PathVariable Long id) {
        try {
            Integer stock = inventoryService.getStock(id);
            return ResponseEntity.ok(Map.of("bookId", id, "stock", stock));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to get inventory: " + e.getMessage()));
        }
    }

    // 管理员专用端点：获取所有书籍（包括已删除的）
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Book>> getAllBooksForAdmin(
            @RequestParam(required = false) String category,
            @RequestParam(name = "title", required = false) String titleKeyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        
        Sort.Direction direction = sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<Book> booksPage;
        if (titleKeyword != null && !titleKeyword.trim().isEmpty()) {
            booksPage = bookService.searchBooksByTitleIncludingDeleted(titleKeyword, pageable);
        } else if (category != null && !category.trim().isEmpty()) {
            booksPage = bookService.getBooksByCategoryIncludingDeleted(category, pageable);
        } else {
            booksPage = bookService.getAllBooksIncludingDeleted(pageable);
        }
        return ResponseEntity.ok(booksPage);
    }

    // 管理员专用端点：获取已删除的书籍
    @GetMapping("/admin/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Book>> getDeletedBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deletedAt,desc") String[] sort) {
        
        Sort.Direction direction = sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        
        Page<Book> deletedBooksPage = bookService.getDeletedBooks(pageable);
        return ResponseEntity.ok(deletedBooksPage);
    }
}