-- ========================================
-- 在线书店数据库初始化脚本
-- 此脚本在MySQL容器首次启动时自动执行
-- 基于真实数据库结构和数据
-- ========================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 使用书店数据库
USE bookstore_db;

-- ========================================
-- 1. 角色表 (roles) - 使用ENUM类型
-- ========================================
CREATE TABLE IF NOT EXISTS `roles` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` ENUM('ROLE_ADMIN','ROLE_MODERATOR','ROLE_USER') NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UKofx66keruapi6vyqpv6f2or37` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初始化角色数据
INSERT IGNORE INTO `roles` (`id`, `name`) VALUES 
    (1, 'ROLE_USER'),
    (2, 'ROLE_ADMIN'),
    (3, 'ROLE_MODERATOR');

-- ========================================
-- 2. 用户表 (users)
-- ========================================
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `email` VARCHAR(50) NOT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `address` TEXT DEFAULT NULL,
    `avatar_url` VARCHAR(255) DEFAULT NULL,
    `enabled` BIT(1) NOT NULL DEFAULT b'1',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_username` (`username`),
    UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 3. 用户认证表 (user_auths)
-- ========================================
CREATE TABLE IF NOT EXISTS `user_auths` (
    `user_id` BIGINT NOT NULL,
    `password` VARCHAR(120) NOT NULL,
    PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_user_auths_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 4. 用户角色关联表 (user_roles)
-- ========================================
CREATE TABLE IF NOT EXISTS `user_roles` (
    `user_id` BIGINT NOT NULL,
    `role_id` INT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 5. 图书表 (books) - 完整结构
-- ========================================
CREATE TABLE IF NOT EXISTS `books` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `author` VARCHAR(100) DEFAULT NULL,
    `category` VARCHAR(50) DEFAULT NULL,
    `cover` VARCHAR(1000) DEFAULT NULL,
    `description` TEXT DEFAULT NULL,
    `price` DECIMAL(10,2) DEFAULT NULL,
    `stock` INT DEFAULT NULL,
    `title` VARCHAR(255) NOT NULL,
    `isbn` VARCHAR(20) DEFAULT NULL,
    `publisher` VARCHAR(100) DEFAULT NULL,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    `created_at` DATETIME(6) DEFAULT NULL,
    `deleted_at` DATETIME(6) DEFAULT NULL,
    `updated_at` DATETIME(6) DEFAULT NULL,
    `tags` JSON DEFAULT NULL COMMENT '图书标签列表',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_books_isbn` (`isbn`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 6. 图书库存表 (book_inventory)
-- ========================================
CREATE TABLE IF NOT EXISTS `book_inventory` (
    `book_id` BIGINT NOT NULL,
    `stock` INT NOT NULL DEFAULT 0,
    `updated_at` DATETIME(6) DEFAULT NULL,
    `version` BIGINT DEFAULT 0,
    PRIMARY KEY (`book_id`),
    KEY `idx_book_inventory_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 7. 购物车项表 (cart_items)
-- ========================================
CREATE TABLE IF NOT EXISTS `cart_items` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `book_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    KEY `fk_cart_items_user` (`user_id`),
    KEY `fk_cart_items_book` (`book_id`),
    CONSTRAINT `fk_cart_items_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cart_items_book` FOREIGN KEY (`book_id`) REFERENCES `books`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 8. 订单表 (orders)
-- ========================================
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `total_price` DECIMAL(10,2) NOT NULL,
    `status` VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    `order_date` DATETIME(6) DEFAULT NULL,
    `shipping_address` TEXT DEFAULT NULL,
    `created_at` DATETIME(6) DEFAULT NULL,
    `updated_at` DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_orders_user` (`user_id`),
    CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 9. 订单项表 (order_items)
-- ========================================
CREATE TABLE IF NOT EXISTS `order_items` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `book_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    `price_at_purchase` DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_order_items_order` (`order_id`),
    KEY `fk_order_items_book` (`book_id`),
    CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_order_items_book` FOREIGN KEY (`book_id`) REFERENCES `books`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 插入真实图书数据
-- ========================================
INSERT INTO `books` (`id`, `author`, `category`, `cover`, `description`, `price`, `stock`, `title`, `isbn`, `publisher`, `deleted`, `created_at`, `deleted_at`, `updated_at`, `tags`) VALUES
(2, 'Randal E. Bryant, David R. O''Hallaron', 'cs-classic', '/images/csapp.jpg', '计算机系统领域经典之作，来深入剖析计算机系统底层原理。', 145.00, 111, '深入理解计算机系统 (第3版)', '9787111512815', '机械工业出版社', b'0', NULL, NULL, NOW(), '[\"技术类\", \"计算机经典\", \"操作系统\"]'),
(3, 'Thomas H. Cormen', 'cs-classic', '/images/clrs.jpg', '算法领域圣经，全面覆盖经典算法和数据结构。', 128.00, 111, '算法导论 (原书第3版)', '9787111407019', '机械工业出版社', b'0', NULL, NULL, NOW(), '[\"技术类\", \"计算机经典\", \"算法\"]'),
(4, 'Matt Frisbie', 'web-dev', '/images/js_ninja.jpg', '前端开发必备红宝书，详细讲解JavaScript语言核心及高级特性。', 129.00, 122, 'JavaScript高级程序设计 (第4版)', '9787115531737', '人民邮电出版社', b'0', NULL, NULL, NOW(), '[\"技术类\", \"Web开发\", \"前端开发\", \"JavaScript\"]'),
(5, 'Eric Matthes', 'programming-lang', '/images/python_crash.jpg', 'Python入门畅销书，通过项目实践引导读者快速掌握Python编程。', 89.00, 223, 'Python编程：从入门到实践', '9787115546038', '人民邮电出版社', b'0', NULL, NULL, NOW(), '[\"技术类\", \"编程语言\", \"Python\"]'),
(6, '刘慈欣', 'sci-fi', '/images/three.jpg', '中国科幻里程碑之作，包含《三体》、《黑暗森林》、《死神永生》三部曲。', 93.00, 123, '三体全集', '9787536692930', '重庆出版社', b'0', NULL, NULL, NOW(), '[\"文学类\", \"小说\", \"科幻小说\"]')
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);

-- ========================================
-- 插入图书库存数据
-- ========================================
INSERT INTO `book_inventory` (`book_id`, `stock`, `updated_at`, `version`) VALUES
(2, 100, NOW(), 0),
(3, 100, NOW(), 0),
(4, 100, NOW(), 0),
(5, 100, NOW(), 0),
(6, 100, NOW(), 0)
ON DUPLICATE KEY UPDATE `stock` = VALUES(`stock`);

-- ========================================
-- 初始化完成
-- ========================================
SELECT '数据库初始化完成!' AS message;
