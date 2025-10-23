package com.bookstore.online_bookstore_backend.repository; // 确保这是您正确的包名

import com.bookstore.online_bookstore_backend.entity.Book; // 导入您的Book实体
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List; // 如果您需要返回列表的方法
import java.util.Optional;

@Repository // 表明这是一个 Spring 管理的 Repository Bean
public interface BookRepository extends JpaRepository<Book, Long> {

    // 重写默认的findAll方法，只返回未删除的书籍
    @Query("SELECT b FROM Book b WHERE b.deleted = false")
    List<Book> findAll();

    // 重写默认的findAll方法（分页），只返回未删除的书籍
    @Query("SELECT b FROM Book b WHERE b.deleted = false")
    Page<Book> findAll(Pageable pageable);

    // 重写默认的findById方法，只返回未删除的书籍
    @Query("SELECT b FROM Book b WHERE b.id = :id AND b.deleted = false")
    Optional<Book> findById(@Param("id") Long id);

    // 按分类查找，只返回未删除的书籍
    @Query("SELECT b FROM Book b WHERE b.category = :category AND b.deleted = false")
    Page<Book> findByCategory(@Param("category") String category, Pageable pageable);

    // 按书名模糊查找，只返回未删除的书籍
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND b.deleted = false")
    Page<Book> findByTitleContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    // 管理员专用：查找所有书籍（包括已删除的）
    @Query("SELECT b FROM Book b")
    Page<Book> findAllIncludingDeleted(Pageable pageable);

    // 管理员专用：按分类查找所有书籍（包括已删除的）
    @Query("SELECT b FROM Book b WHERE b.category = :category")
    Page<Book> findByCategoryIncludingDeleted(@Param("category") String category, Pageable pageable);

    // 管理员专用：按书名模糊查找所有书籍（包括已删除的）
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> findByTitleContainingIgnoreCaseIncludingDeleted(@Param("keyword") String keyword, Pageable pageable);

    // 查找已删除的书籍
    @Query("SELECT b FROM Book b WHERE b.deleted = true")
    Page<Book> findDeletedBooks(Pageable pageable);

    // 检查ISBN是否已存在（排除已删除的书籍）
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Book b WHERE b.isbn = :isbn AND b.deleted = false")
    boolean existsByIsbnAndNotDeleted(@Param("isbn") String isbn);

    // 检查ISBN是否已存在（排除指定ID和已删除的书籍，用于更新时验证）
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Book b WHERE b.isbn = :isbn AND b.id != :id AND b.deleted = false")
    boolean existsByIsbnAndIdNotAndNotDeleted(@Param("isbn") String isbn, @Param("id") Long id);

    // 忽略删除状态查找书籍（用于软删除和恢复操作）
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdIgnoreDeleted(@Param("id") Long id);

}