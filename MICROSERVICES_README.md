# 📚 E-Book 微服务架构文档

## 🎯 项目概述

本项目实现了一个**作者查询微服务**，可以根据书名查询作者信息。使用 Spring Cloud 微服务架构，通过 Eureka 实现服务注册与发现，使用 OpenFeign 实现服务间调用。

---

## 🏗️ 架构组成

### 三个核心模块

| 模块 | 端口 | 职责 |
|------|------|------|
| **Eureka Server** | 8761 | 服务注册中心 |
| **Author Service** | 8081 | 作者查询微服务 |
| **Bookstore Backend** | 8080 | 主业务系统（已集成） |

### 架构图

```
                   ┌─────────────────────┐
                   │   Eureka Server     │
                   │   (端口: 8761)      │
                   └──────────┬──────────┘
                              │
              ┌───────────────┴───────────────┐
              │                               │
    ┌─────────▼──────────┐         ┌─────────▼──────────┐
    │  Author Service    │         │ Bookstore Backend  │
    │  (端口: 8081)      │◄────────│  (端口: 8080)      │
    │                    │ Feign   │                    │
    └─────────┬──────────┘         └─────────┬──────────┘
              │                               │
              └───────────────┬───────────────┘
                              │
                       ┌──────▼────────┐
                       │  MySQL 数据库  │
                       └───────────────┘
```

---

## 📂 项目结构

```
E:\web\
├── eureka-server/                    # 服务注册中心
│   ├── src/main/
│   │   ├── java/com/bookstore/eureka/
│   │   │   └── EurekaServerApplication.java
│   │   └── resources/application.yml
│   └── pom.xml
│
├── author-service/                   # 作者查询微服务
│   ├── src/main/
│   │   ├── java/com/bookstore/author/
│   │   │   ├── AuthorServiceApplication.java
│   │   │   ├── controller/AuthorController.java
│   │   │   ├── service/AuthorService.java
│   │   │   ├── repository/BookRepository.java
│   │   │   ├── entity/Book.java
│   │   │   └── dto/AuthorResponse.java
│   │   └── resources/application.yml
│   └── pom.xml
│
└── online-bookstore-backend/         # 主业务系统（已改造）
    ├── src/main/java/.../
    │   ├── OnlineBookstoreBackendApplication.java  # 已添加 @EnableDiscoveryClient、@EnableFeignClients
    │   ├── client/AuthorServiceClient.java         # Feign 客户端（新增）
    │   ├── dto/AuthorResponseDTO.java              # 响应 DTO（新增）
    │   └── controller/BookController.java          # 已添加作者查询接口
    └── pom.xml                                     # 已添加 Spring Cloud 依赖
```

---

## 🚀 快速启动

### 前置条件
✅ MySQL 数据库已启动 (端口: 3306)  
✅ VSCode 已安装 Java 扩展（Extension Pack for Java）

### 启动方式（三选一）

---

### 🎯 方式一：使用 VSCode 运行配置（推荐）

已为您配置好 `.vscode/launch.json`，可以轻松管理多个服务。

#### 步骤：

1. **按 F5 或点击左侧的运行按钮（▶️）**
2. **在顶部下拉菜单中选择要运行的服务：**
   - `🌐 Eureka Server` - 服务注册中心
   - `📚 Author Service` - 作者查询微服务
   - `🏪 Bookstore Backend` - 主业务系统
   - `🚀 启动所有微服务` - **一键启动所有服务**

3. **点击绿色三角按钮启动**

#### 启动顺序建议：
1. 先启动 `🌐 Eureka Server`（等待 30 秒）
2. 再启动 `📚 Author Service`（等待 15 秒）
3. 最后启动 `🏪 Bookstore Backend`

或者直接选择 `🚀 启动所有微服务` 一键启动（但可能会有启动顺序问题）

---

### 🎯 方式二：手动点击运行

#### 1️⃣ 启动 Eureka Server
1. 打开 `eureka-server/src/main/java/com/bookstore/eureka/EurekaServerApplication.java`
2. 点击文件中的 `Run` 链接
3. 等待启动完成（约 30 秒）
4. **验证：** 访问 http://localhost:8761

#### 2️⃣ 启动 Author Service
1. 打开 `author-service/src/main/java/com/bookstore/author/AuthorServiceApplication.java`
2. 点击文件中的 `Run` 链接
3. 等待启动完成（约 15 秒）
4. **验证：** 在 Eureka Dashboard 看到 `AUTHOR-SERVICE`

#### 3️⃣ 启动 Bookstore Backend
1. 打开 `online-bookstore-backend/src/main/java/.../OnlineBookstoreBackendApplication.java`
2. 点击文件中的 `Run` 链接
3. 等待启动完成（约 20 秒）
4. **验证：** 在 Eureka Dashboard 看到 `BOOKSTORE-BACKEND`

⚠️ **注意**：每次点击 Run 会创建新的运行实例，不会覆盖之前的服务。

---

### 🎯 方式三：使用终端命令

打开 3 个终端窗口，分别执行：

**终端 1 - Eureka Server：**
```bash
cd eureka-server
mvn spring-boot:run
```

**终端 2 - Author Service：**
```bash
cd author-service
mvn spring-boot:run
```

**终端 3 - Bookstore Backend：**
```bash
cd online-bookstore-backend
mvn spring-boot:run
```

⚠️ **注意**：需要已安装 Maven（`mvn --version` 检查）

---

## 📡 API 接口

### 通过主系统调用（推荐）

#### 1. 精确查询作者
```http
GET /api/books/author?title=Spring实战
Host: localhost:8080
```

**响应示例：**
```json
{
  "bookTitle": "Spring实战",
  "author": "Craig Walls",
  "bookId": 1,
  "isbn": "978-7-115-12345-6",
  "publisher": "人民邮电出版社",
  "otherBooks": [
    {
      "id": 2,
      "title": "Spring Boot实战",
      "isbn": "978-7-115-54321-0"
    }
  ]
}
```

#### 2. 模糊查询作者
```http
GET /api/books/author/search?keyword=Java
Host: localhost:8080
```

### 直接调用微服务

#### 健康检查
```http
GET /api/authors/health
Host: localhost:8081
```

#### 精确查询
```http
GET /api/authors/by-book?title=Spring实战
Host: localhost:8081
```

#### 模糊查询
```http
GET /api/authors/search?keyword=Java
Host: localhost:8081
```

---

## 🧪 测试

### 方式一：使用测试脚本
```bash
test-author-service.bat
```

### 方式二：使用 curl
```bash
# 1. 测试健康检查
curl http://localhost:8081/api/authors/health

# 2. 精确查询（通过主系统）
curl "http://localhost:8080/api/books/author?title=Spring实战"

# 3. 模糊查询（通过主系统）
curl "http://localhost:8080/api/books/author/search?keyword=Java"
```

---

## 🔧 技术实现

### 1. Feign 客户端（无需单独下载）

**Maven 会自动下载依赖**，已在 `pom.xml` 中配置：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

**Feign 客户端定义：**
```java
@FeignClient(name = "author-service", path = "/api/authors")
public interface AuthorServiceClient {
    
    @GetMapping("/by-book")
    AuthorResponseDTO getAuthorByBookTitle(@RequestParam("title") String bookTitle);
}
```

### 2. 服务注册

**主应用类：**
```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class OnlineBookstoreBackendApplication {
    // ...
}
```

### 3. 服务调用

**Controller 中调用：**
```java
@Autowired
private AuthorServiceClient authorServiceClient;

@GetMapping("/author")
public ResponseEntity<?> getAuthorByBookTitle(@RequestParam("title") String bookTitle) {
    try {
        AuthorResponseDTO response = authorServiceClient.getAuthorByBookTitle(bookTitle);
        return ResponseEntity.ok(response);
    } catch (FeignException.NotFound e) {
        return ResponseEntity.status(404).body("未找到书籍");
    } catch (FeignException e) {
        return ResponseEntity.status(503).body("服务暂时不可用");
    }
}
```

---

## ⚙️ 配置说明

### Eureka Server 配置
**文件**: `eureka-server/src/main/resources/application.yml`

```yaml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

### Author Service 配置
**文件**: `author-service/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: author-service  # 服务名称（重要！）
  datasource:
    url: jdbc:mysql://localhost:3306/bookstore_db
    username: root
    password: Zy050811

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Bookstore Backend 配置
**文件**: `online-bookstore-backend/src/main/resources/application.properties`

```properties
spring.application.name=bookstore-backend
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

---

## ❌ 故障排除

### 问题1：Feign 依赖无法下载

**解决方案：**
1. 在 VSCode 中右键点击 `pom.xml`
2. 选择 "Update project" 或 "Reload project"
3. Maven 会自动下载所有依赖（包括 Feign）

如果网络慢，可以配置国内 Maven 镜像：
```xml
<!-- 在 pom.xml 中添加 -->
<repositories>
    <repository>
        <id>aliyun</id>
        <url>https://maven.aliyun.com/repository/public</url>
    </repository>
</repositories>
```

### 问题2：服务无法注册到 Eureka

**解决方案：**
1. 确保 Eureka Server 已启动
2. 访问 http://localhost:8761 确认服务可访问
3. 检查配置文件中的 `eureka.client.service-url.defaultZone`
4. 等待 30 秒让服务完全注册

### 问题3：Feign 调用失败

**症状：** `Load balancer does not have available server for client: author-service`

**解决方案：**
1. 确认 Author Service 已启动
2. 在 Eureka Dashboard 确认服务状态为 `UP`
3. 等待 30 秒让服务完全注册

### 问题4：数据库连接失败

**解决方案：**
1. 确认 MySQL 已启动
2. 检查数据库配置（用户名、密码）
3. 确认数据库 `bookstore_db` 已创建

---

## 📊 服务端口

| 服务 | 端口 | 地址 |
|------|------|------|
| Eureka Dashboard | 8761 | http://localhost:8761 |
| Author Service | 8081 | http://localhost:8081 |
| Bookstore Backend | 8080 | http://localhost:8080 |

---

## 🎯 核心功能

### Author Service 提供的功能

1. **精确查询作者**
   - 输入：书名
   - 输出：作者信息 + 该作者的其他作品

2. **模糊查询作者**
   - 输入：关键词
   - 输出：匹配的书籍列表及作者信息

3. **健康检查**
   - 检查服务运行状态

### Bookstore Backend 新增接口

1. `GET /api/books/author?title=xxx` - 精确查询
2. `GET /api/books/author/search?keyword=xxx` - 模糊查询

---

## 🎓 技术栈

- **Spring Boot**: 3.2.0 / 3.4.5
- **Spring Cloud**: 2023.0.0
- **Eureka**: 服务注册与发现
- **OpenFeign**: 声明式服务调用（Maven 自动下载）
- **MySQL**: 8.0.41
- **Spring Data JPA**: ORM 框架

---

## ✨ 项目亮点

1. ✅ 完整的微服务架构
2. ✅ 服务自动注册与发现
3. ✅ 声明式服务调用
4. ✅ 完善的异常处理
5. ✅ 易于扩展和维护

---

## 📝 开发说明

### 依赖管理
- **Maven 自动管理**：所有依赖（包括 Feign）都在 `pom.xml` 中定义
- **无需手动下载**：第一次运行时，Maven 会自动下载所有依赖
- **VSCode 集成**：右键点击 `pom.xml` 可以更新项目依赖

### 启动方式
- **推荐**：在 VSCode 中点击 Run 按钮
- **命令行**（如果配置了 Maven）：`mvn spring-boot:run`
- **JAR 方式**：先构建项目，然后 `java -jar target/xxx.jar`

---

**最后更新**: 2025-11-03  
**版本**: 1.0.0
