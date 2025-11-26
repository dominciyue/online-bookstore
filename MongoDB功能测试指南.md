# MongoDB功能测试指南

## 🎯 测试目标

验证MongoDB集成后，所有核心功能是否正常工作，特别是：
- 书籍description从MongoDB正确加载
- 添加/更新书籍时MongoDB数据正确保存
- 下单功能正常，库存管理无误

## 🔐 准备工作

### 1. 重置管理员密码（如需要）

```sql
-- 在MySQL中执行
UPDATE users 
SET password = '$2a$10$EblZqNptyYvcJzXsQvmvqeH8kJYxBvJXkX8BvQvvFvh7F/vUlXvr6' 
WHERE username = 'admin';
```

### 2. 登录获取Token

**使用Postman或前端登录：**
- 用户名：`admin`
- 密码：`admin123`

**API端点：**
```http
POST http://localhost:8080/api/auth/signin
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**保存返回的accessToken**，后续请求都需要。

## 📋 详细测试步骤

### 测试1：查看书籍列表（验证description加载）

#### 1.1 浏览器测试
1. 打开浏览器访问：`http://localhost:3000`
2. 点击任意书籍查看详情
3. **验证点：应该看到完整的书籍描述**

#### 1.2 API测试
```http
GET http://localhost:8080/api/books/2
```

**预期响应：**
```json
{
  "id": 2,
  "title": "深入理解计算机系统 (第3版)",
  "author": "Randal E. Bryant, David R. O'Hallaron",
  "isbn": "9787111512815",
  "publisher": "机械工业出版社",
  "price": 145.00,
  "cover": "/images/csapp.jpg",
  "description": "计算机系统领域经典之作，来深入剖析计算机系统底层原理。",
  "category": "cs-classic",
  "stock": 0
}
```

**✅ 验证点：**
- description字段存在且有值
- 数据从MongoDB加载

#### 1.3 MongoDB验证
打开MongoDB Compass，执行查询：
```javascript
// 在bookstore_mongo.books集合中
db.books.findOne({ bookId: 2 })
```

应该看到对应的文档。

---

### 测试2：添加新书籍（验证双数据源写入）

#### 2.1 API请求
```http
POST http://localhost:8080/api/books
Authorization: Bearer {your_admin_token}
Content-Type: application/json

{
  "title": "Spring Boot 3.x微服务实战",
  "author": "王五",
  "isbn": "9787111888888",
  "publisher": "清华大学出版社",
  "price": 128.00,
  "cover": "/images/springboot3-microservices.jpg",
  "description": "本书全面介绍了Spring Boot 3.x在微服务架构中的应用。涵盖服务注册与发现、配置中心、API网关、链路追踪、熔断降级等核心技术。通过电商系统的完整实战案例，帮助读者掌握微服务架构的设计与实现。全书包含20+个实战项目，代码清晰，注释详尽，适合有一定Spring Boot基础的开发者阅读。",
  "category": "microservices",
  "stock": 50
}
```

#### 2.2 验证MySQL
```sql
SELECT id, title, author, cover, stock, category 
FROM books 
WHERE isbn = '9787111888888';
```

**预期结果：**
- ✅ 新记录已插入
- ✅ **注意：description字段应该为NULL或不存在**（因为标记为@Transient）

#### 2.3 验证MongoDB
在Compass中查询：
```javascript
db.books.find({ bookId: {刚才创建的书籍ID} }).pretty()
```

**预期结果：**
```json
{
  "bookId": 10,
  "cover": "/images/springboot3-microservices.jpg",
  "description": "本书全面介绍了Spring Boot 3.x在微服务架构中的应用...",
  "reviews": [],
  "createdAt": ISODate("..."),
  "updatedAt": ISODate("...")
}
```

**✅ 验证点：**
- MongoDB文档成功创建
- description内容完整保存
- reviews初始化为空数组

---

### 测试3：更新书籍（验证MongoDB同步更新）

#### 3.1 更新书籍信息
```http
PUT http://localhost:8080/api/books/2
Authorization: Bearer {your_admin_token}
Content-Type: application/json

{
  "title": "深入理解计算机系统 (第3版)",
  "author": "Randal E. Bryant, David R. O'Hallaron",
  "isbn": "9787111512815",
  "publisher": "机械工业出版社",
  "price": 149.00,
  "cover": "/images/csapp_new.jpg",
  "description": "【新版更新】计算机系统领域经典之作，深入剖析计算机系统底层原理。新增第15章：优化程序性能，以及第16章：系统级I/O。适合计算机专业学生和系统程序员阅读。",
  "category": "cs-classic",
  "stock": 20
}
```

#### 3.2 验证MySQL更新
```sql
SELECT id, title, price, cover, stock 
FROM books 
WHERE id = 2;
```

**✅ 验证点：**
- price更新为149.00
- cover更新为新路径
- stock更新为20

#### 3.3 验证MongoDB更新
```javascript
db.books.findOne({ bookId: 2 })
```

**✅ 验证点：**
- description已更新为新内容
- updatedAt时间戳已更新

---

### 测试4：下单流程（验证库存管理）

#### 4.1 查看初始库存
```http
GET http://localhost:8080/api/books/4
```

记录当前stock值（例如：9）

#### 4.2 添加到购物车
```http
POST http://localhost:8080/api/cart
Authorization: Bearer {user_token}
Content-Type: application/json

{
  "bookId": 4,
  "quantity": 2
}
```

#### 4.3 创建订单
```http
POST http://localhost:8080/api/orders/checkout
Authorization: Bearer {user_token}
Content-Type: application/json

{
  "shippingAddress": "北京市朝阳区测试路123号"
}
```

#### 4.4 验证库存扣减
```http
GET http://localhost:8080/api/books/4
```

**✅ 验证点：**
- stock减少2（例如：9 → 7）
- 订单创建成功
- MongoDB数据不受影响

#### 4.5 MySQL验证
```sql
SELECT id, title, stock FROM books WHERE id = 4;
```

**✅ 验证点：**
- stock字段正确减少

---

### 测试5：软删除功能

#### 5.1 软删除书籍
```http
DELETE http://localhost:8080/api/books/9
Authorization: Bearer {admin_token}
```

#### 5.2 验证MySQL
```sql
SELECT id, title, deleted, deleted_at 
FROM books 
WHERE id = 9;
```

**✅ 验证点：**
- deleted = true
- deleted_at有时间戳

#### 5.3 验证MongoDB
```javascript
db.books.findOne({ bookId: 9 })
```

**✅ 验证点：**
- MongoDB文档仍然存在（保留历史）

#### 5.4 验证前端
访问：`http://localhost:3000`

**✅ 验证点：**
- 书籍列表中不再显示ID为9的书

---

### 测试6：搜索功能

#### 6.1 按标题搜索
```http
GET http://localhost:8080/api/books?title=Python&page=0&size=10
```

**✅ 验证点：**
- 返回包含"Python"的书籍
- 每本书都包含description字段

#### 6.2 按分类查询
```http
GET http://localhost:8080/api/books?category=sci-fi&page=0&size=10
```

**✅ 验证点：**
- 返回sci-fi分类的书籍
- 数据完整

---

### 测试7：管理员统计功能

```http
GET http://localhost:8080/api/admin/statistics/books?startDate=2025-10-01T00:00:00&endDate=2025-11-30T23:59:59
Authorization: Bearer {admin_token}
```

**✅ 验证点：**
- 返回销售统计数据
- 包含cover字段
- 数据准确

---

## 🔧 常见问题排查

### 问题1：description字段为null

**原因：** MongoDB中没有对应的bookId文档

**解决：**
```javascript
// 在MongoDB Compass中手动插入
db.books.insertOne({
  bookId: {对应的MySQL ID},
  cover: "",
  description: "手动添加的描述",
  reviews: [],
  createdAt: new Date(),
  updatedAt: new Date()
})
```

### 问题2：应用启动失败

**检查：**
```powershell
# 1. MongoDB服务是否运行
Get-Service MongoDB

# 2. 如果未运行，启动它
Start-Service MongoDB
```

### 问题3：图片不显示

**原因：** cover字段路径不正确

**解决：**
- 确保图片文件在`public/images/`目录下
- 或使用完整URL路径

---

## ✅ 完整测试检查清单

- [ ] 应用成功启动，无错误
- [ ] 访问首页，书籍列表正常显示
- [ ] 点击书籍详情，能看到完整描述
- [ ] 添加新书籍，MySQL和MongoDB都有数据
- [ ] 更新书籍，两个数据源都同步更新
- [ ] 添加购物车功能正常
- [ ] 创建订单，库存正确减少
- [ ] 软删除书籍，前端不再显示
- [ ] MongoDB中有6条初始文档
- [ ] bookId唯一索引已创建
- [ ] description全文索引已创建

全部通过后，MongoDB集成即完成！

