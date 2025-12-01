package com.bookstore.online_bookstore_backend.service;

import com.bookstore.online_bookstore_backend.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务
 * 提供图书信息和库存的缓存操作
 * 实现缓存降级机制，当 Redis 不可用时自动降级
 */
@Service
public class RedisCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheService.class);
    
    // 缓存 key 前缀
    private static final String BOOK_CACHE_PREFIX = "book:";
    private static final String BOOK_INVENTORY_PREFIX = "inventory:";
    private static final String BOOK_LIST_CACHE_PREFIX = "book:list:";
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${bookstore.cache.enabled:true}")
    private boolean cacheEnabled;
    
    @Value("${bookstore.cache.book-ttl:7200}")
    private long bookTtl;
    
    private volatile boolean redisAvailable = true;
    
    /**
     * 检查 Redis 是否可用
     */
    public boolean isRedisAvailable() {
        if (!cacheEnabled) {
            return false;
        }
        
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            if (!redisAvailable) {
                logger.info("✅ Redis connection restored");
                redisAvailable = true;
            }
            return true;
        } catch (Exception e) {
            if (redisAvailable) {
                logger.error("❌ Redis connection failed, system degraded to database: {}", e.getMessage());
                redisAvailable = false;
            }
            return false;
        }
    }
    
    /**
     * 缓存图书信息
     */
    public void cacheBook(Book book) {
        if (!isRedisAvailable() || book == null) {
            return;
        }
        
        try {
            String key = BOOK_CACHE_PREFIX + book.getId();
            redisTemplate.opsForValue().set(key, book, bookTtl, TimeUnit.SECONDS);
            logger.debug("📦 Book cached: ID={}, Title={}", book.getId(), book.getTitle());
        } catch (Exception e) {
            logger.warn("⚠️ Failed to cache book: {}", e.getMessage());
            redisAvailable = false;
        }
    }
    
    /**
     * 获取缓存的图书信息
     */
    public Book getCachedBook(Long bookId) {
        if (!isRedisAvailable() || bookId == null) {
            return null;
        }
        
        try {
            String key = BOOK_CACHE_PREFIX + bookId;
            Object obj = redisTemplate.opsForValue().get(key);
            if (obj != null) {
                logger.debug("🎯 Cache hit: BookID={}", bookId);
                return (Book) obj;
            } else {
                logger.debug("❌ Cache miss: BookID={}", bookId);
            }
        } catch (Exception e) {
            logger.warn("⚠️ Failed to get cached book: {}", e.getMessage());
            redisAvailable = false;
        }
        return null;
    }
    
    /**
     * 删除图书缓存
     */
    public void evictBook(Long bookId) {
        if (!isRedisAvailable() || bookId == null) {
            return;
        }
        
        try {
            String key = BOOK_CACHE_PREFIX + bookId;
            redisTemplate.delete(key);
            logger.debug("🗑️ Book cache evicted: ID={}", bookId);
        } catch (Exception e) {
            logger.warn("⚠️ Failed to evict book cache: {}", e.getMessage());
        }
    }
    
    /**
     * 缓存库存信息
     */
    public void cacheInventory(Long bookId, Integer stock) {
        if (!isRedisAvailable() || bookId == null) {
            return;
        }
        
        try {
            String key = BOOK_INVENTORY_PREFIX + bookId;
            redisTemplate.opsForValue().set(key, stock, bookTtl, TimeUnit.SECONDS);
            logger.debug("📦 Inventory cached: BookID={}, Stock={}", bookId, stock);
        } catch (Exception e) {
            logger.warn("⚠️ Failed to cache inventory: {}", e.getMessage());
            redisAvailable = false;
        }
    }
    
    /**
     * 获取缓存的库存信息
     */
    public Integer getCachedInventory(Long bookId) {
        if (!isRedisAvailable() || bookId == null) {
            return null;
        }
        
        try {
            String key = BOOK_INVENTORY_PREFIX + bookId;
            Object obj = redisTemplate.opsForValue().get(key);
            if (obj != null) {
                logger.debug("🎯 Inventory cache hit: BookID={}", bookId);
                return (Integer) obj;
            } else {
                logger.debug("❌ Inventory cache miss: BookID={}", bookId);
            }
        } catch (Exception e) {
            logger.warn("⚠️ Failed to get cached inventory: {}", e.getMessage());
            redisAvailable = false;
        }
        return null;
    }
    
    /**
     * 更新库存缓存（原子操作）
     */
    public boolean updateInventoryCache(Long bookId, int delta) {
        if (!isRedisAvailable() || bookId == null) {
            return false;
        }
        
        try {
            String key = BOOK_INVENTORY_PREFIX + bookId;
            Long newValue = redisTemplate.opsForValue().increment(key, delta);
            logger.debug("📊 Inventory cache updated: BookID={}, Delta={}, NewValue={}", bookId, delta, newValue);
            return newValue != null && newValue >= 0;
        } catch (Exception e) {
            logger.warn("⚠️ Failed to update inventory cache: {}", e.getMessage());
            redisAvailable = false;
            return false;
        }
    }
    
    /**
     * 删除库存缓存
     */
    public void evictInventory(Long bookId) {
        if (!isRedisAvailable() || bookId == null) {
            return;
        }
        
        try {
            String key = BOOK_INVENTORY_PREFIX + bookId;
            redisTemplate.delete(key);
            logger.debug("🗑️ Inventory cache evicted: BookID={}", bookId);
        } catch (Exception e) {
            logger.warn("⚠️ Failed to evict inventory cache: {}", e.getMessage());
        }
    }
    
    /**
     * 清除所有图书相关缓存
     */
    public void evictAllBookCaches() {
        if (!isRedisAvailable()) {
            return;
        }
        
        try {
            // 这里可以使用 scan 命令批量删除，但为了简单起见暂时不实现
            logger.info("🗑️ Triggered to evict all book caches");
        } catch (Exception e) {
            logger.warn("⚠️ Failed to evict all caches: {}", e.getMessage());
        }
    }
    
    /**
     * 设置通用缓存
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        if (!isRedisAvailable()) {
            return;
        }
        
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            logger.warn("⚠️ Failed to set cache: key={}, error={}", key, e.getMessage());
            redisAvailable = false;
        }
    }
    
    /**
     * 获取通用缓存
     */
    public Object get(String key) {
        if (!isRedisAvailable()) {
            return null;
        }
        
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.warn("⚠️ Failed to get cache: key={}, error={}", key, e.getMessage());
            redisAvailable = false;
            return null;
        }
    }
    
    /**
     * 删除缓存
     */
    public void delete(String key) {
        if (!isRedisAvailable()) {
            return;
        }
        
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            logger.warn("⚠️ Failed to delete cache: key={}, error={}", key, e.getMessage());
        }
    }
}

