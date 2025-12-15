# GraphQL 使用指南

## 前端访问

### 方式1：通过导航菜单

1. 启动前端应用：`npm start`
2. 访问：`http://localhost:3000`
3. 在左侧导航栏点击 **"GraphQL 搜索"** 菜单项（带有 API 图标）
4. 进入 GraphQL 搜索页面

### 方式2：直接访问 URL

直接在浏览器访问：`http://localhost:3000/graphql-search`

## 功能演示

### 搜索书籍

1. 在搜索框输入书名关键词（如 "Java"、"Spring"、"Python"）
2. 点击 **"搜索"** 按钮或按 **Enter** 键
3. 查看搜索结果

### 分页浏览

- 点击 **"上一页"** / **"下一页"** 按钮切换页面
- 页面显示当前页码和总页数

### 查看书籍详情

- 每本书显示：封面、书名、作者、分类、价格、ISBN
- 鼠标悬停在书籍卡片上会有动画效果

## 后端测试

### 使用 GraphiQL UI

1. 访问：`http://localhost:8080/graphiql`
2. 在左侧输入查询：

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

3. 在 "Query Variables" 面板输入：

```json
{
  "title": "Java",
  "page": 0,
  "size": 10
}
```

4. 点击运行按钮（▶️）执行查询

### 自定义查询字段

GraphQL 的优势在于可以只请求需要的字段：

```graphql
query SearchBooksSimple($title: String!) {
  searchBooksByTitle(title: $title, page: 0, size: 5) {
    content {
      title
      author
      price
    }
    totalElements
  }
}
```

这样只返回书名、作者和价格，减少数据传输。

## 高级用法

### 同时查询多个书籍

```graphql
query MultipleQueries {
  javaBooks: searchBooksByTitle(title: "Java", page: 0, size: 5) {
    content {
      id
      title
      price
    }
    totalElements
  }
  
  pythonBooks: searchBooksByTitle(title: "Python", page: 0, size: 5) {
    content {
      id
      title
      price
    }
    totalElements
  }
}
```

### 查询单本书详情

```graphql
query GetBook {
  getBookById(id: "1") {
    id
    title
    author
    isbn
    publisher
    price
    description
    category
  }
}
```

## 与 REST API 对比

### REST API 查询
```
GET /api/books?title=Java&page=0&size=10
```
- 固定端点
- 返回所有字段
- 无法自定义返回数据

### GraphQL 查询
```graphql
query {
  searchBooksByTitle(title: "Java", page: 0, size: 10) {
    content { id title price }
    totalElements
  }
}
```
- 单一端点 `/graphql`
- 只返回请求的字段
- 灵活自定义

## 性能优化建议

### 1. 只请求需要的字段
```graphql
# 好 - 只请求必要字段
query {
  searchBooksByTitle(title: "Java") {
    content { id title price }
  }
}

# 不好 - 请求所有字段
query {
  searchBooksByTitle(title: "Java") {
    content {
      id title author isbn publisher
      price cover description category
      createdAt updatedAt deletedAt
    }
  }
}
```

### 2. 合理设置分页大小
```graphql
# 推荐：每页 10-20 条
searchBooksByTitle(title: "Java", size: 10)

# 不推荐：一次请求太多数据
searchBooksByTitle(title: "Java", size: 100)
```

### 3. 使用变量复用查询
```javascript
// 定义一次查询
const SEARCH_QUERY = `
  query SearchBooks($title: String!, $page: Int, $size: Int) {
    searchBooksByTitle(title: $title, page: $page, size: $size) {
      content { id title price }
      totalElements
    }
  }
`;

// 多次使用，只改变变量
executeQuery(SEARCH_QUERY, { title: "Java", page: 0, size: 10 });
executeQuery(SEARCH_QUERY, { title: "Python", page: 0, size: 10 });
executeQuery(SEARCH_QUERY, { title: "Java", page: 1, size: 10 });
```

## 常见问题

### 1. 搜索无结果

**原因**：数据库中没有匹配的书籍

**解决**：
- 确保数据库有数据
- 尝试更宽泛的搜索词
- 检查书名是否正确

### 2. GraphiQL 无法访问

**原因**：GraphiQL 未启用或被安全策略阻止

**解决**：
```properties
# 检查 application.properties
spring.graphql.graphiql.enabled=true
```

### 3. 前端请求失败

**原因**：CORS 配置或后端未启动

**解决**：
- 确认后端在运行：`http://localhost:8080/graphql`
- 检查浏览器控制台错误信息
- 验证 CORS 配置是否正确

### 4. 查询语法错误

**原因**：GraphQL 查询语法不正确

**解决**：
- 在 GraphiQL 中验证查询
- 检查变量类型是否匹配
- 确保字段名称正确

## 开发技巧

### 1. 使用 GraphiQL 测试

在开发时，先在 GraphiQL 中测试查询，确认无误后再复制到前端代码。

### 2. 查看 Schema 文档

在 GraphiQL 右侧的 "Docs" 面板可以查看完整的 Schema 定义和可用查询。

### 3. 使用浏览器开发工具

在浏览器 Network 面板中查看 GraphQL 请求和响应，便于调试。

### 4. 错误处理

```javascript
try {
  const result = await searchBooksByTitleGraphQL(title, page, size);
  // 处理成功结果
} catch (error) {
  console.error('GraphQL Error:', error);
  // 显示错误信息给用户
}
```

## 扩展学习

### 推荐资源

- [GraphQL 官方文档](https://graphql.org/learn/)
- [Spring for GraphQL](https://spring.io/projects/spring-graphql)
- [GraphQL Best Practices](https://graphql.org/learn/best-practices/)

### 下一步学习

1. 实现更复杂的查询（嵌套查询、片段）
2. 添加 GraphQL Mutation（修改数据）
3. 实现 GraphQL Subscription（实时更新）
4. 使用 DataLoader 优化性能
5. 添加查询复杂度限制

