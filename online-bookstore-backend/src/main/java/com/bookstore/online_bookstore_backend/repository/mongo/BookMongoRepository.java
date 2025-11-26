package com.bookstore.online_bookstore_backend.repository.mongo;

import com.bookstore.online_bookstore_backend.entity.mongo.BookMongoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB Repository - 管理书籍富媒体数据
 */
@Repository
public interface BookMongoRepository extends MongoRepository<BookMongoDocument, String> {
    
    /**
     * 根据MySQL的book_id查找MongoDB文档
     * @param bookId MySQL中的书籍ID
     * @return MongoDB文档
     */
    Optional<BookMongoDocument> findByBookId(Long bookId);
    
    /**
     * 批量根据bookId查找
     * @param bookIds 书籍ID列表
     * @return MongoDB文档列表
     */
    List<BookMongoDocument> findByBookIdIn(List<Long> bookIds);
    
    /**
     * 根据bookId删除MongoDB文档
     * @param bookId MySQL中的书籍ID
     */
    void deleteByBookId(Long bookId);
    
    /**
     * 检查bookId是否存在
     * @param bookId MySQL中的书籍ID
     * @return 是否存在
     */
    boolean existsByBookId(Long bookId);
    
    /**
     * 全文搜索（需要text索引）
     * @param keyword 搜索关键词
     * @return 匹配的文档列表
     */
    @Query("{ $text: { $search: ?0 } }")
    List<BookMongoDocument> searchByText(String keyword);
}

