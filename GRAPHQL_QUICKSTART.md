# GraphQL 快速启动指南

## 启动步骤

### 1. 更新 Maven 依赖

```bash
cd E:\web\online-bookstore-backend
.\mvnw.cmd clean install
```

### 2. 启动后端服务

```bash
cd E:\web\online-bookstore-backend
.\mvnw.cmd spring-boot:run
```

或使用 IDE 直接运行 `OnlineBookstoreBackendApplication`

### 3. 验证 GraphQL 端点

访问 GraphiQL UI：`http://localhost:8080/graphiql`

### 4. 测试 GraphQL 查询

在 GraphiQL 中输入以下查询：

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

**变量**（在 GraphiQL 的 Query Variables 面板中）：
```json
{
  "title": "Java",
  "page": 0,
  "size": 10
}
```

### 5. 启动前端

```bash
cd E:\web
npm install  # 首次运行或依赖更新后
npm start
```

访问方式：
- **直接访问**：`http://localhost:3000/graphql-search`
- **通过导航**：访问 `http://localhost:3000` 后，点击左侧菜单的 "GraphQL 搜索"

## 快速测试命令

### 使用 curl 测试

```bash
curl -X POST http://localhost:8080/graphql ^
  -H "Content-Type: application/json" ^
  -d "{\"query\":\"query{searchBooksByTitle(title:\\\"Java\\\",page:0,size:10){content{id title author price}totalElements}}\"}"
```

### 使用 PowerShell 测试

```powershell
$body = @{
    query = "query SearchBooks(`$title: String!, `$page: Int, `$size: Int) { searchBooksByTitle(title: `$title, page: `$page, size: `$size) { content { id title author price } totalElements } }"
    variables = @{
        title = "Java"
        page = 0
        size = 10
    }
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Body $body -ContentType "application/json"
```

## 常见问题

### 1. 端口被占用

如果 8080 端口被占用，修改 `application.properties`：
```properties
server.port=8081
```

### 2. Maven 依赖下载失败

清理并重新下载：
```bash
.\mvnw.cmd clean
.\mvnw.cmd dependency:resolve
.\mvnw.cmd install
```

### 3. GraphiQL 无法访问

检查 Spring Security 配置是否允许 `/graphiql` 访问。

### 4. 查询返回空结果

确保数据库中有数据，可以先通过 REST API 添加测试数据。

## 文件清单

### 后端文件
- `pom.xml` - 添加了 GraphQL 依赖
- `src/main/resources/graphql/schema.graphqls` - GraphQL Schema
- `src/main/resources/application.properties` - GraphQL 配置
- `src/main/java/.../graphql/BookQueryResolver.java` - 查询解析器
- `src/main/java/.../graphql/BookPageDTO.java` - 分页 DTO
- `src/main/java/.../config/WebSecurityConfig.java` - 安全配置更新

### 前端文件
- `src/services/graphqlService.js` - GraphQL 查询服务
- `src/pages/GraphQLSearchPage.js` - 搜索页面组件
- `src/pages/GraphQLSearchPage.css` - 样式文件

## 下一步

1. 在 `App.js` 中添加路由到 GraphQL 搜索页面
2. 尝试修改查询，只返回需要的字段
3. 测试不同的搜索关键词和分页参数
4. 查看 `GRAPHQL_IMPLEMENTATION.md` 了解详细实现

## 技术支持

- GraphQL 官方文档：https://graphql.org/
- Spring for GraphQL：https://spring.io/projects/spring-graphql
- GraphiQL：https://github.com/graphql/graphiql

