-- ============================================
-- Neo4j标签搜索功能 - MySQL数据准备脚本
-- ============================================

USE bookstore_db;

-- 1. 验证tags字段是否存在
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'bookstore_db' 
  AND TABLE_NAME = 'books' 
  AND COLUMN_NAME = 'tags';

-- 2. 查看当前图书的tags数据
SELECT id, title, category, tags 
FROM books 
WHERE deleted = 0;

-- 3. 如果tags字段为空，执行以下更新（根据你的数据调整）
-- 注意：确保标签名称与Neo4j中的标签名称完全一致

UPDATE books 
SET tags = JSON_ARRAY('技术类', '计算机经典', '操作系统') 
WHERE id = 2 AND title LIKE '%计算机系统%';

UPDATE books 
SET tags = JSON_ARRAY('技术类', '计算机经典', '算法') 
WHERE id = 3 AND title LIKE '%算法导论%';

UPDATE books 
SET tags = JSON_ARRAY('技术类', 'Web开发', '前端开发', 'JavaScript') 
WHERE id = 4 AND title LIKE '%JavaScript%';

UPDATE books 
SET tags = JSON_ARRAY('技术类', '编程语言', 'Python') 
WHERE id = 5 AND title LIKE '%Python%';

UPDATE books 
SET tags = JSON_ARRAY('文学类', '小说', '科幻小说') 
WHERE id = 6 AND title LIKE '%三体%';

UPDATE books 
SET tags = JSON_ARRAY('文学类', '小说', '科幻小说') 
WHERE id = 9 AND category = 'sci-fi';

-- 4. 验证更新结果
SELECT id, title, category, tags 
FROM books 
WHERE deleted = 0 AND tags IS NOT NULL;

-- 5. 测试JSON_TABLE查询（验证MySQL是否支持）
SELECT b.id, b.title, jt.tag_value
FROM books b
JOIN JSON_TABLE(
    b.tags, '$[*]' COLUMNS(tag_value VARCHAR(255) PATH '$')
) AS jt
WHERE b.deleted = false AND b.tags IS NOT NULL;

-- 6. 测试按标签搜索（模拟后端查询）
SELECT b.* 
FROM books b 
WHERE b.deleted = false 
  AND b.tags IS NOT NULL 
  AND EXISTS (
      SELECT 1 FROM JSON_TABLE(
          b.tags, '$[*]' COLUMNS(tag_value VARCHAR(255) PATH '$')
      ) AS jt 
      WHERE jt.tag_value IN ('技术类', '编程语言', 'Python')
  );

-- 7. 查看每本书的标签详情
SELECT 
    id,
    title,
    JSON_LENGTH(tags) as tag_count,
    JSON_EXTRACT(tags, '$[0]') as tag1,
    JSON_EXTRACT(tags, '$[1]') as tag2,
    JSON_EXTRACT(tags, '$[2]') as tag3
FROM books 
WHERE deleted = 0 AND tags IS NOT NULL;

-- 注意：如果第5步测试失败，说明MySQL版本不支持JSON_TABLE，需要使用替代方案

