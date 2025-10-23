package com.bookstore.online_bookstore_backend.controller; // 确保是正确的包名

import com.bookstore.online_bookstore_backend.entity.Book;
import com.bookstore.online_bookstore_backend.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*; // 导入所有 Web 注解

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController // 组合了 @Controller 和 @ResponseBody，表示所有方法返回的数据直接写入HTTP响应体
@RequestMapping("/api/books") // 此 Controller 处理的所有请求都以 /api/books 为前缀
public class BookController {

    private final BookService bookService;

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

    // GET /api/books/{id} - 根据ID获取书籍详情
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Optional<Book> bookOptional = bookService.getBookById(id);
        // 如果找到了书，返回 200 OK 和书对象；否则返回 404 Not Found
        return bookOptional.map(ResponseEntity::ok)
                           .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/books - 添加一本新书
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        try {
            // @RequestBody 将HTTP请求体中的JSON数据绑定到Book对象
            Book savedBook = bookService.saveBook(book);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBook); // 返回 201 Created 和保存后的书对象
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // PUT /api/books/{id} - 更新已有书籍信息
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody Book bookDetails) {
        try {
            Optional<Book> existingBookOptional = bookService.getBookById(id);
            if (existingBookOptional.isPresent()) {
                Book existingBook = existingBookOptional.get();
                // 更新字段 (这里简单地用传入对象的字段覆盖，实际应用中可能需要更复杂的合并逻辑)
                existingBook.setTitle(bookDetails.getTitle());
                existingBook.setAuthor(bookDetails.getAuthor());
                existingBook.setIsbn(bookDetails.getIsbn());
                existingBook.setPublisher(bookDetails.getPublisher());
                existingBook.setPrice(bookDetails.getPrice());
                existingBook.setCover(bookDetails.getCover());
                existingBook.setDescription(bookDetails.getDescription());
                existingBook.setCategory(bookDetails.getCategory());
                existingBook.setStock(bookDetails.getStock());
                // 注意：bookDetails传过来的id可能会被忽略，因为我们用的是existingBook的id
                Book updatedBook = bookService.saveBook(existingBook); // save 方法也可以用于更新
                return ResponseEntity.ok(updatedBook);
            } else {
                return ResponseEntity.notFound().build(); // 如果找不到要更新的书，返回404
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // DELETE /api/books/{id} - 根据ID软删除书籍
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBookById(id); // 使用软删除
            return ResponseEntity.ok(Map.of("message", "书籍已成功删除（软删除）"));
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
            return ResponseEntity.ok(Map.of("message", "书籍已成功恢复"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    // DELETE /api/books/{id}/hard - 物理删除书籍（管理员专用）
    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> hardDeleteBook(@PathVariable Long id) {
        try {
            Optional<Book> bookOptional = bookService.getBookById(id);
            if (bookOptional.isPresent()) {
                bookService.hardDeleteBookById(id);
                return ResponseEntity.ok(Map.of("message", "书籍已永久删除"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "删除失败: " + e.getMessage()));
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