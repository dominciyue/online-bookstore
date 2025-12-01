package com.bookstore.online_bookstore_backend.dao;

import com.bookstore.online_bookstore_backend.entity.BookInventory;
import java.util.Optional;

public interface BookInventoryDao {
    /**
     * 根据图书ID查询库存
     */
    Optional<BookInventory> findByBookId(Long bookId);
    
    /**
     * 根据图书ID查询库存（带锁）
     */
    Optional<BookInventory> findByBookIdWithLock(Long bookId);
    
    /**
     * 保存或更新库存
     */
    BookInventory save(BookInventory inventory);
    
    /**
     * 删除库存记录
     */
    void deleteByBookId(Long bookId);
    
    /**
     * 减少库存（原子操作）
     * @return 是否成功
     */
    boolean reduceStock(Long bookId, int quantity);
    
    /**
     * 增加库存（原子操作）
     */
    void addStock(Long bookId, int quantity);
}

