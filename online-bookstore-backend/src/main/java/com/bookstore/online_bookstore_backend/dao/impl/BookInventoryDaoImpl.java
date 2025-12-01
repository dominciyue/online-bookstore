package com.bookstore.online_bookstore_backend.dao.impl;

import com.bookstore.online_bookstore_backend.dao.BookInventoryDao;
import com.bookstore.online_bookstore_backend.entity.BookInventory;
import com.bookstore.online_bookstore_backend.repository.BookInventoryRepository;
import com.bookstore.online_bookstore_backend.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class BookInventoryDaoImpl implements BookInventoryDao {
    
    private static final Logger logger = LoggerFactory.getLogger(BookInventoryDaoImpl.class);
    
    @Autowired
    private BookInventoryRepository inventoryRepository;
    
    @Autowired
    private RedisCacheService redisCacheService;
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BookInventory> findByBookId(Long bookId) {
        // 1. Try Redis cache first
        Integer cachedStock = redisCacheService.getCachedInventory(bookId);
        if (cachedStock != null) {
            logger.info("✅ Inventory from Redis: BookID={}, Stock={}", bookId, cachedStock);
            BookInventory inventory = new BookInventory();
            inventory.setBookId(bookId);
            inventory.setStock(cachedStock);
            return Optional.of(inventory);
        }
        
        // 2. Cache miss, query database
        logger.info("⚠️ Redis miss, query DB: BookID={}", bookId);
        Optional<BookInventory> inventoryOpt = inventoryRepository.findById(bookId);
        
        // 3. Cache to Redis if found
        inventoryOpt.ifPresent(inv -> {
            redisCacheService.cacheInventory(bookId, inv.getStock());
            logger.info("📦 Cached to Redis: BookID={}, Stock={}", bookId, inv.getStock());
        });
        
        return inventoryOpt;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BookInventory> findByBookIdWithLock(Long bookId) {
        return inventoryRepository.findByIdWithLock(bookId);
    }
    
    @Override
    @Transactional
    public BookInventory save(BookInventory inventory) {
        // 1. 保存到数据库
        BookInventory saved = inventoryRepository.save(inventory);
        
        // 2. 更新 Redis 缓存
        redisCacheService.cacheInventory(saved.getBookId(), saved.getStock());
        logger.info("✅ Inventory saved and cached: BookID={}, Stock={}", saved.getBookId(), saved.getStock());
        
        return saved;
    }
    
    @Override
    @Transactional
    public void deleteByBookId(Long bookId) {
        // 1. Delete from database
        inventoryRepository.deleteById(bookId);
        
        // 2. Evict from Redis
        redisCacheService.evictInventory(bookId);
        logger.info("✅ Inventory deleted: BookID={}", bookId);
    }
    
    @Override
    @Transactional
    public boolean reduceStock(Long bookId, int quantity) {
        logger.info("Attempt to reduce stock: BookID={}, Quantity={}", bookId, quantity);
        
        try {
            // 1. 先尝试从 Redis 减库存（原子操作）
            if (redisCacheService.isRedisAvailable()) {
                boolean success = redisCacheService.updateInventoryCache(bookId, -quantity);
                if (success) {
                    // Redis 操作成功，异步更新数据库
                    Optional<BookInventory> inventoryOpt = inventoryRepository.findByIdWithLock(bookId);
                    if (inventoryOpt.isPresent()) {
                        BookInventory inventory = inventoryOpt.get();
                        if (inventory.reduceStock(quantity)) {
                            inventoryRepository.save(inventory);
                            logger.info("✅ Stock reduced successfully (Redis+DB): BookID={}, NewStock={}", bookId, inventory.getStock());
                            return true;
                        } else {
                            // 数据库库存不足，回滚 Redis
                            redisCacheService.updateInventoryCache(bookId, quantity);
                            logger.warn("⚠️ DB stock insufficient, Redis rolled back: BookID={}", bookId);
                            return false;
                        }
                    }
                }
            }
            
            // 2. Redis 不可用，直接操作数据库
            logger.warn("⚠️ Redis unavailable, operate DB directly");
            Optional<BookInventory> inventoryOpt = inventoryRepository.findByIdWithLock(bookId);
            if (inventoryOpt.isPresent()) {
                BookInventory inventory = inventoryOpt.get();
                if (inventory.reduceStock(quantity)) {
                    inventoryRepository.save(inventory);
                    logger.info("✅ Stock reduced successfully (DB only): BookID={}, NewStock={}", bookId, inventory.getStock());
                    return true;
                } else {
                    logger.warn("❌ Insufficient stock: BookID={}, CurrentStock={}, RequiredQuantity={}", 
                               bookId, inventory.getStock(), quantity);
                    return false;
                }
            } else {
                logger.error("❌ Inventory record not found: BookID={}", bookId);
                return false;
            }
        } catch (Exception e) {
            logger.error("❌ Failed to reduce stock: BookID={}, Error={}", bookId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public void addStock(Long bookId, int quantity) {
        logger.info("Add stock: BookID={}, Quantity={}", bookId, quantity);
        
        // 1. 更新数据库
        Optional<BookInventory> inventoryOpt = inventoryRepository.findById(bookId);
        if (inventoryOpt.isPresent()) {
            BookInventory inventory = inventoryOpt.get();
            inventory.addStock(quantity);
            inventoryRepository.save(inventory);
            
            // 2. 更新 Redis 缓存
            redisCacheService.updateInventoryCache(bookId, quantity);
            logger.info("✅ Stock added successfully: BookID={}, NewStock={}", bookId, inventory.getStock());
        } else {
            // 如果不存在，创建新记录
            BookInventory newInventory = new BookInventory();
            newInventory.setBookId(bookId);
            newInventory.setStock(quantity);
            inventoryRepository.save(newInventory);
            redisCacheService.cacheInventory(bookId, quantity);
            logger.info("✅ Inventory record created: BookID={}, Stock={}", bookId, quantity);
        }
    }
}

