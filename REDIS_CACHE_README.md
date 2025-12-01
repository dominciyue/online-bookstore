# Redis 缓存实现文档

## 一、项目概述

本项目在原有的在线书店系统基础上，集成了 Redis 缓存功能，实现了以下目标：

1. **数据库重构**：将图书库存信息从 `books` 表分离到独立的 `book_inventory` 表
2. **Redis 缓存集成**：对图书基础信息和库存信息进行缓存
3. **缓存降级机制**：Redis 宕机时自动降级到数据库查询
4. **完善的日志系统**：记录所有缓存操作和数据库操作

## 二、架构设计

### 2.1 数据库表结构

#### books 表（图书基础信息）
- 存储图书的基本信息：标题、作者、ISBN、出版社、价格、封面、描述、分类等
- 这些数据变化频率较低，适合长期缓存（TTL: 7200秒）

#### book_inventory 表（库存信息）
- 仅存储 `book_id`、`stock`、`version`、`updated_at`
- 使用乐观锁（version 字段）防止并发更新冲突
- 高频变化数据，适合 Redis 原子操作

### 2.2 Redis 缓存策略

#### 缓存 Key 设计
```
book:{id}           - 图书基础信息
inventory:{id}      - 库存数量
```

#### 缓存更新策略
- **Write-Through**：写操作同时更新数据库和 Redis
- **Cache-Aside**：读操作先查 Redis，未命中则查数据库并回写缓存
- **TTL 设置**：图书信息 7200 秒，防止缓存永久占用内存

#### 容错机制
- Redis 连接失败时自动降级到数据库
- 使用 `redisAvailable` 标志位快速判断 Redis 状态
- 所有 Redis 操作都包裹在 try-catch 中，避免影响主业务

## 三、核心代码文件

### 3.1 新增文件

| 文件路径 | 说明 |
|---------|------|
| `entity/BookInventory.java` | 库存实体类，包含乐观锁 |
| `repository/BookInventoryRepository.java` | 库存 Repository，支持悲观锁查询 |
| `dao/BookInventoryDao.java` | 库存 DAO 接口 |
| `dao/impl/BookInventoryDaoImpl.java` | 库存 DAO 实现，集成 Redis 缓存 |
| `service/BookInventoryService.java` | 库存服务类 |
| `service/RedisCacheService.java` | Redis 缓存服务，提供缓存操作和降级逻辑 |
| `config/RedisConfig.java` | Redis 配置类，配置序列化策略 |
| `dto/BookWithInventoryDTO.java` | 整合图书和库存信息的 DTO |
| `resources/logback-spring.xml` | 日志配置文件 |
| `resources/db_migration_add_inventory.sql` | 数据库迁移脚本 |

### 3.2 修改文件

| 文件路径 | 修改内容 |
|---------|---------|
| `pom.xml` | 添加 Redis 相关依赖 |
| `entity/Book.java` | 移除 stock 字段（已迁移到 BookInventory） |
| `dao/impl/BookDaoImpl.java` | 集成 Redis 缓存逻辑 |
| `service/BookService.java` | 添加获取带库存信息的方法 |
| `controller/BookController.java` | 适配新的 DTO 和库存服务 |
| `resources/application.properties` | 添加 Redis 配置 |

## 四、部署和测试步骤

### 4.1 环境准备

#### 1. 启动 Redis
```bash
cd e:\web\Redis-x64-3.0.504
# 方式1：使用批处理文件
start-redis.bat

# 方式2：直接命令
redis-server.exe redis.windows.conf
```

#### 2. 测试 Redis 连接
```bash
# 使用测试脚本
test-redis.bat

# 或直接使用 redis-cli
redis-cli.exe
> ping
PONG
```

#### 3. 数据库迁移
```sql
-- 在 MySQL 中执行迁移脚本
source e:/web/online-bookstore-backend/src/main/resources/db_migration_add_inventory.sql
```

### 4.2 启动应用

```bash
cd e:\web\online-bookstore-backend
mvn spring-boot:run
```

### 4.3 测试场景

#### 测试1：首次读取（数据库 → 缓存）
```bash
# 请求：GET /api/books/1
# 预期日志：
⚠️ Redis未命中，从数据库查询图书: ID=1
📦 图书信息已缓存到Redis: ID=1, Title=...
```

#### 测试2：后续读取（缓存命中）
```bash
# 请求：GET /api/books/1
# 预期日志：
🎯 缓存命中: 图书ID=1
✅ 从Redis获取图书信息: ID=1, Title=...
```

#### 测试3：更新操作（缓存同步）
```bash
# 请求：PUT /api/books/1
# 预期日志：
保存图书信息: ID=1, Title=...
✅ 图书信息已保存并缓存: ID=1, Title=...
```

#### 测试4：Redis 宕机测试
```bash
# 1. 停止 Redis 服务（在 Redis 窗口按 Ctrl+C）
# 2. 请求：GET /api/books/1
# 预期日志：
❌ Redis 连接失败，系统将降级到数据库查询: ...
⚠️ Redis未命中，从数据库查询图书: ID=1
# 3. 应用仍然正常返回数据，只是不再使用缓存
```

#### 测试5：Redis 恢复
```bash
# 1. 重新启动 Redis
# 2. 请求：GET /api/books/1
# 预期日志：
✅ Redis 连接已恢复
⚠️ Redis未命中，从数据库查询图书: ID=1
📦 图书信息已缓存到Redis: ID=1, Title=...
```

#### 测试6：库存操作
```bash
# 请求：PUT /api/books/1/inventory
# Body: {"stock": 100}
# 预期日志：
保存库存信息: BookID=1, Stock=100
✅ 库存信息已保存并缓存: BookID=1, Stock=100
```

## 五、日志输出说明

### 5.1 日志文件位置

```
online-bookstore-backend/logs/
├── redis.log          - Redis 缓存相关日志
├── database.log       - 数据库操作日志
└── business.log       - 业务逻辑日志
```

### 5.2 日志符号说明

| 符号 | 含义 |
|------|------|
| ✅ | 操作成功 |
| ❌ | 操作失败/错误 |
| ⚠️ | 警告/降级 |
| 🎯 | 缓存命中 |
| 📦 | 数据缓存 |
| 🗑️ | 缓存删除 |
| 📊 | 数据更新 |

### 5.3 关键日志示例

#### 缓存命中
```
2025-10-27 14:30:15.123 [http-nio-8080-exec-1] DEBUG RedisCacheService - 📦 图书信息已缓存: ID=1, Title=Java编程思想
2025-10-27 14:30:20.456 [http-nio-8080-exec-2] DEBUG RedisCacheService - 🎯 缓存命中: 图书ID=1
2025-10-27 14:30:20.458 [http-nio-8080-exec-2] INFO  BookDaoImpl - ✅ 从Redis获取图书信息: ID=1, Title=Java编程思想
```

#### Redis 宕机
```
2025-10-27 14:35:10.789 [http-nio-8080-exec-3] ERROR RedisCacheService - ❌ Redis 连接失败，系统将降级到数据库查询: Connection refused
2025-10-27 14:35:10.790 [http-nio-8080-exec-3] WARN  BookDaoImpl - ⚠️ Redis未命中，从数据库查询图书: ID=1
```

#### 数据更新
```
2025-10-27 14:40:25.321 [http-nio-8080-exec-4] INFO  BookDaoImpl - 保存图书信息: ID=1, Title=Java编程思想（第4版）
2025-10-27 14:40:25.456 [http-nio-8080-exec-4] INFO  BookDaoImpl - ✅ 图书信息已保存并缓存: ID=1, Title=Java编程思想（第4版）
```

## 六、Redis 监控命令

### 6.1 查看缓存数据
```bash
# 进入 Redis CLI
redis-cli.exe

# 查看所有图书缓存 key
> KEYS book:*

# 查看所有库存缓存 key
> KEYS inventory:*

# 查看某个图书的缓存内容
> GET book:1

# 查看某个库存值
> GET inventory:1

# 查看 key 的剩余过期时间（秒）
> TTL book:1

# 查看所有 key 的数量
> DBSIZE

# 清空所有缓存（谨慎使用！）
> FLUSHDB
```

### 6.2 监控缓存命中率
```bash
# 查看 Redis 统计信息
> INFO stats

# 重点关注：
# keyspace_hits: 缓存命中次数
# keyspace_misses: 缓存未命中次数
# 命中率 = keyspace_hits / (keyspace_hits + keyspace_misses)
```

## 七、性能优化建议

### 7.1 当前配置
- 图书信息缓存 TTL: 7200秒（2小时）
- 库存信息缓存 TTL: 7200秒（2小时）
- Redis 连接池大小: 8

### 7.2 优化方向
1. **根据实际访问模式调整 TTL**
   - 热门图书可以延长缓存时间
   - 冷门图书可以缩短缓存时间

2. **实现缓存预热**
   - 应用启动时预先加载热门图书到缓存

3. **批量操作优化**
   - 使用 Redis Pipeline 减少网络往返

4. **缓存更新策略**
   - 对于频繁更新的数据，考虑使用 Redis 的 INCR/DECR 原子操作

## 八、常见问题

### Q1: Redis 连接失败怎么办？
**A**: 系统已实现自动降级，会直接查询数据库。检查 Redis 是否启动：
```bash
redis-cli.exe ping
```

### Q2: 如何清空所有缓存？
**A**: 
```bash
redis-cli.exe
> FLUSHDB
```

### Q3: 数据库和缓存不一致怎么办？
**A**: 系统使用 Write-Through 策略，同时更新数据库和缓存。如果出现不一致，可以：
1. 重启应用（缓存会自动重建）
2. 手动清空 Redis 缓存

### Q4: 如何验证缓存是否生效？
**A**: 
1. 查看日志中的 "🎯 缓存命中" 消息
2. 使用 Redis CLI 查看 key: `redis-cli.exe GET book:1`
3. 使用 Redis Monitor: `redis-cli.exe MONITOR`

## 九、作业提交清单

### 9.1 代码文件
- [x] 所有新增和修改的 Java 文件
- [x] pom.xml（包含 Redis 依赖）
- [x] application.properties（包含 Redis 配置）
- [x] logback-spring.xml（日志配置）
- [x] 数据库迁移脚本

### 9.2 文档
- [x] 本 README 文档
- [ ] Word 文档（包含以下内容）：
  - 系统架构设计说明
  - Redis 缓存策略说明
  - 数据库重构说明
  - 测试场景和日志截图：
    - [ ] 首次读取（数据库查询 + 缓存写入）
    - [ ] 后续读取（缓存命中）
    - [ ] 更新操作（缓存同步）
    - [ ] Redis 宕机时的降级
    - [ ] Redis 恢复后的正常运行
  - 日志输出解释

### 9.3 测试截图建议
1. **首次访问**：显示 "Redis未命中，从数据库查询" + "图书信息已缓存"
2. **缓存命中**：显示 "缓存命中" + "从Redis获取图书信息"
3. **更新操作**：显示 "图书信息已保存并缓存"
4. **Redis 宕机**：显示 "Redis 连接失败，系统将降级"
5. **Redis 恢复**：显示 "Redis 连接已恢复"
6. **Redis CLI**：显示缓存 key 和内容
7. **数据库表结构**：显示 book_inventory 表

## 十、总结

本项目成功实现了：
1. ✅ 数据库表结构重构，将库存信息分离
2. ✅ Redis 缓存集成，提高查询性能
3. ✅ 完善的日志系统，便于调试和监控
4. ✅ 缓存降级机制，保证系统高可用
5. ✅ Write-Through 缓存策略，保证数据一致性

通过 Redis 缓存，系统在读多写少的场景下性能可提升 **10-100 倍**，同时通过降级机制保证了系统的稳定性和可用性。

