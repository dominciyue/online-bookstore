package com.bookstore.online_bookstore_backend.service;

import com.bookstore.online_bookstore_backend.dao.BookInventoryDao;
import com.bookstore.online_bookstore_backend.entity.BookInventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 图书库存服务类
 */
@Service
public class BookInventoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookInventoryService.class);
    
    @Autowired
    private BookInventoryDao inventoryDao;
    
    /**
     * 获取图书库存
     */
    @Transactional(readOnly = true)
    public Optional<BookInventory> getInventory(Long bookId) {
        return inventoryDao.findByBookId(bookId);
    }
    
    /**
     * Get stock quantity (optimized with Redis cache)
     */
    @Transactional(readOnly = true)
    public Integer getStock(Long bookId) {
        Optional<BookInventory> inventory = inventoryDao.findByBookId(bookId);
        return inventory.map(BookInventory::getStock).orElse(0);
    }
    
    /**
     * 更新库存
     */
    @Transactional
    public BookInventory updateInventory(Long bookId, Integer stock) {
        Optional<BookInventory> inventoryOpt = inventoryDao.findByBookId(bookId);
        
        BookInventory inventory;
        if (inventoryOpt.isPresent()) {
            inventory = inventoryOpt.get();
            inventory.setStock(stock);
        } else {
            inventory = new BookInventory();
            inventory.setBookId(bookId);
            inventory.setStock(stock);
        }
        
        return inventoryDao.save(inventory);
    }
    
    /**
     * 减少库存
     */
    @Transactional
    public boolean reduceStock(Long bookId, int quantity) {
        return inventoryDao.reduceStock(bookId, quantity);
    }
    
    /**
     * 增加库存
     */
    @Transactional
    public void addStock(Long bookId, int quantity) {
        inventoryDao.addStock(bookId, quantity);
    }
    
    /**
     * 删除库存记录
     */
    @Transactional
    public void deleteInventory(Long bookId) {
        inventoryDao.deleteByBookId(bookId);
    }
}

