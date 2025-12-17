# 修复 GraphQL 401 错误

## 问题描述

前端访问 GraphQL 端点时收到 401 未授权错误。

## 原因

Spring Security 配置中 `/error` 和 `/favicon.ico` 端点未配置为公开访问，导致错误页面也需要认证。

## 已修复内容

在 `WebSecurityConfig.java` 中添加了以下配置：

```java
.requestMatchers("/graphql", "/graphql/**").permitAll()
.requestMatchers("/graphiql", "/graphiql/**").permitAll()
.requestMatchers("/error").permitAll()
.requestMatchers("/favicon.ico").permitAll()
```

## 解决步骤

### 1. 停止后端服务

如果后端正在运行，按 `Ctrl+C` 停止。

### 2. 清理并重新编译

```bash
cd E:\web\online-bookstore-backend
.\mvnw.cmd clean compile
```

### 3. 重新启动后端

```bash
.\mvnw.cmd spring-boot:run
```

或在 IDE 中重新启动应用。

### 4. 验证配置

#### 方法1：使用浏览器

访问：`http://localhost:8080/graphiql`

应该能够直接访问，不需要登录。

#### 方法2：使用 curl 测试

```bash
curl -X POST http://localhost:8080/graphql ^
  -H "Content-Type: application/json" ^
  -d "{\"query\":\"query{searchBooksByTitle(title:\\\"Java\\\",page:0,size:5){content{id title}totalElements}}\"}"
```

应该返回正常的 JSON 数据，而不是 401 错误。

#### 方法3：使用 PowerShell

```powershell
$body = @{
    query = "query { searchBooksByTitle(title: `"Java`", page: 0, size: 5) { content { id title } totalElements } }"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Body $body -ContentType "application/json"
```

### 5. 前端测试

1. 确保前端正在运行：`npm start`
2. 访问：`http://localhost:3000/graphql-search`
3. 输入搜索关键词，点击搜索
4. 应该能看到搜索结果，不再有 401 错误

## 检查清单

- [ ] 后端已停止并重新启动
- [ ] GraphiQL UI 可以访问（`http://localhost:8080/graphiql`）
- [ ] curl 测试返回正常数据
- [ ] 前端搜索功能正常工作
- [ ] 浏览器控制台没有 401 错误

## 如果问题仍然存在

### 1. 检查后端日志

查看是否还有 "Unauthorized error" 或 "Full authentication is required" 的错误。

### 2. 清理 Maven 缓存

```bash
cd E:\web\online-bookstore-backend
.\mvnw.cmd clean
rd /s /q target
.\mvnw.cmd compile
.\mvnw.cmd spring-boot:run
```

### 3. 验证配置文件

确认 `WebSecurityConfig.java` 中包含以下内容：

```java
.requestMatchers("/graphql", "/graphql/**").permitAll()
.requestMatchers("/graphiql", "/graphiql/**").permitAll()
.requestMatchers("/error").permitAll()
.requestMatchers("/favicon.ico").permitAll()
```

### 4. 检查端口

确保后端运行在 8080 端口：

```bash
netstat -ano | findstr :8080
```

### 5. 浏览器缓存

清除浏览器缓存或使用无痕模式测试。

## 测试查询示例

### GraphiQL 中测试

```graphql
query TestSearch {
  searchBooksByTitle(title: "Java", page: 0, size: 5) {
    content {
      id
      title
      author
      price
    }
    totalElements
    totalPages
  }
}
```

### 预期响应

```json
{
  "data": {
    "searchBooksByTitle": {
      "content": [
        {
          "id": "1",
          "title": "Java编程思想",
          "author": "Bruce Eckel",
          "price": 99.0
        }
      ],
      "totalElements": 1,
      "totalPages": 1
    }
  }
}
```

## 常见错误信息

### 错误1：401 Unauthorized

```
搜索失败: HTTP error! status: 401
```

**原因**：Spring Security 拦截了请求

**解决**：确保已重新启动后端，配置已生效

### 错误2：404 Not Found

```
搜索失败: HTTP error! status: 404
```

**原因**：GraphQL 端点不存在

**解决**：
- 检查 `application.properties` 中 GraphQL 配置
- 确认 `spring-boot-starter-graphql` 依赖已添加
- 重新编译项目

### 错误3：500 Internal Server Error

**原因**：后端处理查询时出错

**解决**：
- 查看后端详细错误日志
- 检查 GraphQL Resolver 是否正确
- 验证数据库连接

## 调试技巧

### 1. 启用详细日志

在 `application.properties` 中添加：

```properties
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.graphql=DEBUG
```

### 2. 使用 GraphiQL 调试

GraphiQL 提供了更好的错误信息和自动补全功能。

### 3. 浏览器开发工具

打开浏览器开发工具（F12），查看：
- Network 面板：查看请求和响应
- Console 面板：查看 JavaScript 错误

## 总结

修改 `WebSecurityConfig.java` 后，必须重新编译并重启后端服务才能使配置生效。确保 `/graphql`、`/graphiql`、`/error` 和 `/favicon.ico` 都配置为公开访问。


