package com.bookstore.online_bookstore_backend.repository;

import com.bookstore.online_bookstore_backend.entity.BookInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface BookInventoryRepository extends JpaRepository<BookInventory, Long> {
    
    /**
     * 使用悲观锁查询库存，防止并发问题
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT bi FROM BookInventory bi WHERE bi.bookId = :bookId")
    Optional<BookInventory> findByIdWithLock(@Param("bookId") Long bookId);
}

