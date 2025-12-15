# GraphQL 实现文档

## 概述

本项目实现了使用 GraphQL 按书名查询书籍的功能，完全复用了现有的 DAO、Service、Repository 和 Entity 层代码。

## 实现架构

```
前端 (React)
    ↓
GraphQL 查询 (使用变量)
    ↓
Spring Boot GraphQL 端点 (/graphql)
    ↓
BookQueryResolver (GraphQL Resolver)
    ↓
BookService (复用现有业务逻辑)
    ↓
BookDao → BookRepository
    ↓
数据库 (MySQL)
```

## 后端实现

### 1. 添加依赖 (pom.xml)

```xml
<!-- GraphQL Dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-graphql</artifactId>
</dependency>
<dependency>
    <groupId>com.graphql-java</groupId>
    <artifactId>graphql-java-extended-scalars</artifactId>
    <version>21.0</version>
</dependency>
```

### 2. GraphQL Schema (schema.graphqls)

位置：`src/main/resources/graphql/schema.graphqls`

定义了查询类型和返回类型：
- `searchBooksByTitle`: 按书名搜索，支持分页
- `getBookById`: 根据ID获取书籍详情
- `BookPage`: 分页结果类型
- `Book`: 书籍类型

### 3. GraphQL Resolver (BookQueryResolver.java)

位置：`src/main/java/com/bookstore/online_bookstore_backend/graphql/BookQueryResolver.java`

**关键特性**：
- 使用 `@Controller` 注解标记为 GraphQL 控制器
- 使用 `@QueryMapping` 注解映射 GraphQL 查询
- 使用 `@Argument` 注解接收查询参数
- **完全复用** `BookService.searchBooksByTitle()` 方法
- 支持参数默认值和验证

**复用的现有代码**：
```java
// 复用 BookService
Page<Book> bookPage = bookService.searchBooksByTitle(title, pageable);
```

### 4. DTO 类 (BookPageDTO.java)

位置：`src/main/java/com/bookstore/online_bookstore_backend/graphql/BookPageDTO.java`

封装 Spring Data 的 `Page<Book>` 对象，转换为 GraphQL 可返回的格式。

### 5. 配置 (application.properties)

```properties
# GraphQL Configuration
spring.graphql.graphiql.enabled=true
spring.graphql.graphiql.path=/graphiql
spring.graphql.path=/graphql
spring.graphql.schema.printer.enabled=true
spring.graphql.schema.locations=classpath:graphql/
spring.graphql.schema.file-extensions=.graphqls,.gqls
```

### 6. Spring Security 配置

在 `WebSecurityConfig.java` 中添加：
```java
.requestMatchers("/graphql").permitAll() // 允许访问 GraphQL 端点
.requestMatchers("/graphiql").permitAll() // 允许访问 GraphiQL UI
```

CORS 配置：
```java
source.registerCorsConfiguration("/graphql", configuration);
source.registerCorsConfiguration("/graphiql", configuration);
```

## 前端实现

### 1. GraphQL Service (graphqlService.js)

位置：`src/services/graphqlService.js`

**核心功能**：
- `searchBooksByTitleGraphQL()`: 按书名搜索书籍
- `getBookByIdGraphQL()`: 根据ID获取书籍详情
- `searchBooksSimple()`: 简化版搜索

**使用变量的查询示例**：
```javascript
const query = `
  query SearchBooksByTitle($title: String!, $page: Int, $size: Int) {
    searchBooksByTitle(title: $title, page: $page, size: $size) {
      content {
        id
        title
        author
        price
      }
      totalElements
    }
  }
`;

const variables = {
  title: "Java",
  page: 0,
  size: 10
};
```

### 2. GraphQL 搜索页面 (GraphQLSearchPage.js)

位置：`src/pages/GraphQLSearchPage.js`

**功能特性**：
- 搜索框输入书名
- 支持回车键搜索
- 显示搜索结果（卡片式布局）
- 分页导航
- 显示 GraphQL 查询示例代码
- 响应式设计

## 代码复用说明

### 完全复用的现有代码

1. **Entity 层**
   - `Book.java` - 书籍实体，无需修改

2. **Repository 层**
   - `BookRepository.java` - 数据访问接口
   - `findByTitleContainingIgnoreCase()` 方法

3. **DAO 层**
   - `BookDao.java` 和 `BookDaoImpl.java`
   - `findByTitleContainingIgnoreCase()` 方法

4. **Service 层**
   - `BookService.java`
   - `searchBooksByTitle()` 方法

### 新增代码

1. **GraphQL 层**
   - `BookQueryResolver.java` - GraphQL 查询解析器
   - `BookPageDTO.java` - 分页结果 DTO
   - `schema.graphqls` - GraphQL Schema 定义

2. **前端**
   - `graphqlService.js` - GraphQL 查询服务
   - `GraphQLSearchPage.js` - 搜索页面组件

## 使用方法

### 后端测试

#### 1. 使用 GraphiQL UI

访问：`http://localhost:8080/graphiql`

**查询示例**：
```graphql
query SearchBooks($title: String!, $page: Int, $size: Int) {
  searchBooksByTitle(title: $title, page: $page, size: $size) {
    content {
      id
      title
      author
      price
      cover
      category
    }
    totalElements
    totalPages
    number
  }
}
```

**变量**：
```json
{
  "title": "Java",
  "page": 0,
  "size": 10
}
```

#### 2. 使用 Postman

**URL**: `http://localhost:8080/graphql`

**Method**: POST

**Headers**:
```
Content-Type: application/json
```

**Body** (raw JSON):
```json
{
  "query": "query SearchBooks($title: String!, $page: Int, $size: Int) { searchBooksByTitle(title: $title, page: $page, size: $size) { content { id title author price } totalElements } }",
  "variables": {
    "title": "Spring",
    "page": 0,
    "size": 10
  }
}
```

#### 3. 使用 curl

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query SearchBooks($title: String!, $page: Int, $size: Int) { searchBooksByTitle(title: $title, page: $page, size: $size) { content { id title author price } totalElements } }",
    "variables": {
      "title": "Java",
      "page": 0,
      "size": 10
    }
  }'
```

### 前端使用

#### 1. 访问 GraphQL 搜索页面

在 `App.js` 中添加路由：
```javascript
import GraphQLSearchPage from './pages/GraphQLSearchPage';

// 在路由配置中添加
<Route path="/graphql-search" element={<GraphQLSearchPage />} />
```

访问：`http://localhost:3000/graphql-search`

#### 2. 在其他组件中使用

```javascript
import { searchBooksByTitleGraphQL } from '../services/graphqlService';

// 在组件中使用
const handleSearch = async () => {
  try {
    const result = await searchBooksByTitleGraphQL('Java', 0, 10);
    console.log('找到', result.totalElements, '本书');
    console.log('书籍列表:', result.content);
  } catch (error) {
    console.error('搜索失败:', error);
  }
};
```

## GraphQL vs REST API 对比

| 特性 | GraphQL | REST API |
|------|---------|----------|
| 端点 | 单一端点 `/graphql` | 多个端点 `/api/books/search` |
| 数据获取 | 客户端指定需要的字段 | 服务端固定返回所有字段 |
| 查询复用 | 使用变量复用查询语句 | 需要构造不同的 URL |
| 类型安全 | Schema 定义强类型 | 依赖文档或 Swagger |
| 过度获取 | 不会，只返回请求的字段 | 可能返回不需要的字段 |
| 获取不足 | 不会，一次查询获取所有需要的数据 | 可能需要多次请求 |

## 优势

1. **代码复用**：完全复用现有的业务逻辑，无需重复编写
2. **灵活查询**：客户端可以精确控制返回的字段
3. **类型安全**：Schema 定义提供强类型保证
4. **单一端点**：简化 API 管理
5. **变量支持**：查询语句可复用，只需改变变量值
6. **自文档化**：GraphiQL 提供交互式文档

## 注意事项

1. **性能考虑**：
   - GraphQL 查询可能导致 N+1 问题
   - 使用 DataLoader 或批量查询优化
   - 限制查询深度和复杂度

2. **安全性**：
   - 验证查询复杂度
   - 限制查询深度
   - 实施速率限制
   - 考虑添加认证授权

3. **缓存**：
   - GraphQL 查询不易缓存（POST 请求）
   - 考虑使用持久化查询
   - 实施应用层缓存

## 扩展建议

1. **添加更多查询**：
   - 按分类查询书籍
   - 按价格范围查询
   - 组合条件查询

2. **添加 Mutation**：
   - 添加书籍
   - 更新书籍
   - 删除书籍

3. **添加 Subscription**：
   - 实时书籍更新通知
   - 库存变化推送

4. **性能优化**：
   - 实现 DataLoader
   - 添加查询缓存
   - 使用批量查询

## 总结

本实现成功地将 GraphQL 集成到现有的电子书店项目中，完全复用了现有的数据访问层和业务逻辑层代码。通过使用变量，前端可以复用同一个查询语句，只需改变变量值即可实现不同的搜索需求。这种实现方式既保持了代码的简洁性，又提供了 GraphQL 的灵活性和强大功能。

