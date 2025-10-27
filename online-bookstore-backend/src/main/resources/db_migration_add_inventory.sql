-- ============================================
-- 数据库迁移脚本：将库存信息从 books 表分离
-- ============================================

-- 1. 创建新的 book_inventory 表
CREATE TABLE IF NOT EXISTS `book_inventory` (
  `book_id` BIGINT NOT NULL PRIMARY KEY,
  `stock` INT NOT NULL DEFAULT 0,
  `version` BIGINT NOT NULL DEFAULT 0,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`book_id`) REFERENCES `books`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书库存表';

-- 2. 将现有的库存数据从 books 表迁移到 book_inventory 表
INSERT INTO `book_inventory` (`book_id`, `stock`, `version`, `updated_at`)
SELECT `id`, COALESCE(`stock`, 0), 0, NOW()
FROM `books`
WHERE `deleted` = FALSE
ON DUPLICATE KEY UPDATE 
  `stock` = VALUES(`stock`),
  `updated_at` = VALUES(`updated_at`);

-- 3. (可选) 从 books 表中删除 stock 列
-- 注意：只有在确认数据迁移成功后才执行此步骤
-- ALTER TABLE `books` DROP COLUMN `stock`;

-- 4. 为库存表添加索引
CREATE INDEX `idx_book_inventory_updated_at` ON `book_inventory`(`updated_at`);

-- 验证数据迁移
SELECT 
    b.id,
    b.title,
    b.stock AS old_stock,
    bi.stock AS new_stock
FROM books b
LEFT JOIN book_inventory bi ON b.id = bi.book_id
WHERE b.deleted = FALSE
LIMIT 10;

-- 查看统计信息
SELECT 
    COUNT(*) AS total_books,
    SUM(CASE WHEN bi.book_id IS NOT NULL THEN 1 ELSE 0 END) AS books_with_inventory,
    SUM(CASE WHEN bi.book_id IS NULL THEN 1 ELSE 0 END) AS books_without_inventory
FROM books b
LEFT JOIN book_inventory bi ON b.id = bi.book_id
WHERE b.deleted = FALSE;

