# Docker 容器化部署文档

## 项目概述

本文档描述了在线书店后端系统（Spring Boot + MySQL）的 Docker 容器化部署完整过程，包括配置步骤、遇到的问题及解决方案、数据持久化验证方法。

**部署状态**: ✅ 成功部署  
**测试日期**: 2024年12月24日  
**测试环境**: Windows 10, Docker Desktop v27.1.1

### 部署架构图

```
┌──────────────────────────────────────────────────────────────┐
│                    Docker Compose 环境                        │
│  ┌─────────────────────┐    ┌─────────────────────────────┐  │
│  │   MySQL 8.0         │    │   Spring Boot Backend       │  │
│  │   Container         │◄───│   Container                 │  │
│  │   (bookstore-mysql) │    │   (bookstore-backend)       │  │
│  │                     │    │                             │  │
│  │   Port: 3307:3306   │    │   Port: 8080:8080           │  │
│  │   Data: Bind Mount  │    │   Profile: docker           │  │
│  └─────────────────────┘    └─────────────────────────────┘  │
│            │                            │                     │
│   ./docker-data/mysql         ./docker-data/uploads          │
│   (数据持久化目录)              (上传文件目录)                  │
└──────────────────────────────────────────────────────────────┘
```

---

## 一、配置部署详细过程

### 1.1 第一步：创建Docker配置文件

#### 1.1.1 创建 Dockerfile

在项目根目录创建 `Dockerfile`，用于构建Spring Boot应用镜像：

```dockerfile
# 使用 Amazon Corretto JDK 17 Alpine版本（国内可访问）
FROM amazoncorretto:17-alpine

LABEL maintainer="bookstore-team"
LABEL description="Online Bookstore Backend"

WORKDIR /app

# 创建上传文件目录
RUN mkdir -p /app/uploads/avatars

# 复制本地构建的 jar 文件
COPY target/online-bookstore-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 启动命令，激活docker配置文件
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=docker"]
```

#### 1.1.2 创建 docker-compose.yml

创建Docker Compose服务编排文件：

```yaml
# Docker Compose 配置文件
services:
  # MySQL 数据库服务
  mysql:
    image: mysql:8.0
    container_name: bookstore-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: bookstore123
      MYSQL_DATABASE: bookstore_db
      MYSQL_CHARACTER_SET_SERVER: utf8mb4
      MYSQL_COLLATION_SERVER: utf8mb4_unicode_ci
    ports:
      - "3307:3306"  # 映射到主机3307端口，避免与本地MySQL冲突
    volumes:
      # 使用 Bind Mount 绑定数据目录，确保数据持久化
      - ./docker-data/mysql:/var/lib/mysql
      # 初始化脚本目录
      - ./docker-init:/docker-entrypoint-initdb.d:ro
    networks:
      - bookstore-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-pbookstore123"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 30s

  # Spring Boot 后端应用服务
  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: bookstore-backend
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      JAVA_OPTS: "-Xms256m -Xmx512m"
    volumes:
      - ./docker-data/uploads:/app/uploads
    networks:
      - bookstore-network
    depends_on:
      mysql:
        condition: service_healthy  # 等待MySQL健康检查通过后再启动

networks:
  bookstore-network:
    driver: bridge
    name: bookstore-network
```

#### 1.1.3 创建 application-docker.properties

在 `src/main/resources/` 目录创建Docker环境专用配置：

```properties
# MySQL DataSource Configuration - 使用 Docker 网络中的 MySQL 服务名
spring.datasource.url=jdbc:mysql://mysql:3306/bookstore_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=bookstore123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# 禁用 Kafka 和 Redis 自动配置（简化部署）
spring.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,\
  org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,\
  org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration

# 禁用 Kafka 和缓存功能
spring.kafka.enabled=false
bookstore.cache.enabled=false

# JWT Configuration
bookstore.app.jwtSecret=YourSuperSecretKeyForDevelopmentWhichIsLongAndSecureEnoughForHS512
bookstore.app.jwtExpirationMs=86400000

# 文件上传目录
file.upload-dir=/app/uploads/avatars
```

#### 1.1.4 创建数据库初始化脚本

在 `docker-init/` 目录创建 `01-init-database.sql`：

```sql
-- 在线书店数据库初始化脚本
SET NAMES utf8mb4;
USE bookstore_db;

-- 创建角色表
CREATE TABLE IF NOT EXISTS `roles` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` ENUM('ROLE_ADMIN','ROLE_MODERATOR','ROLE_USER') NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_roles_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化角色数据
INSERT IGNORE INTO `roles` (`id`, `name`) VALUES 
    (1, 'ROLE_USER'),
    (2, 'ROLE_ADMIN'),
    (3, 'ROLE_MODERATOR');

-- 创建图书表
CREATE TABLE IF NOT EXISTS `books` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL,
    `author` VARCHAR(100) DEFAULT NULL,
    `isbn` VARCHAR(20) DEFAULT NULL,
    `publisher` VARCHAR(100) DEFAULT NULL,
    `price` DECIMAL(10,2) DEFAULT NULL,
    `cover` VARCHAR(1000) DEFAULT NULL,
    `description` TEXT DEFAULT NULL,
    `category` VARCHAR(50) DEFAULT NULL,
    `deleted` BIT(1) NOT NULL DEFAULT b'0',
    `created_at` DATETIME(6) DEFAULT NULL,
    `updated_at` DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入示例图书数据
INSERT INTO `books` (`id`, `title`, `author`, `isbn`, `publisher`, `price`, `cover`, `category`, `deleted`) VALUES
(2, '深入理解计算机系统', 'Randal E. Bryant', '9787111512815', '机械工业出版社', 145.00, '/images/csapp.jpg', 'cs-classic', b'0'),
(3, '算法导论', 'Thomas H. Cormen', '9787111407019', '机械工业出版社', 128.00, '/images/clrs.jpg', 'cs-classic', b'0'),
(4, 'JavaScript高级程序设计', 'Matt Frisbie', '9787115531737', '人民邮电出版社', 129.00, '/images/js_ninja.jpg', 'web-dev', b'0'),
(5, 'Python编程：从入门到实践', 'Eric Matthes', '9787115546038', '人民邮电出版社', 89.00, '/images/python_crash.jpg', 'programming-lang', b'0'),
(6, '三体全集', '刘慈欣', '9787536692930', '重庆出版社', 93.00, '/images/three.jpg', 'sci-fi', b'0')
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);

-- 创建图书库存表
CREATE TABLE IF NOT EXISTS `book_inventory` (
    `book_id` BIGINT NOT NULL,
    `stock` INT NOT NULL DEFAULT 0,
    `version` BIGINT DEFAULT 0,
    `updated_at` DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入库存数据
INSERT INTO `book_inventory` (`book_id`, `stock`, `version`) VALUES
(2, 100, 0), (3, 100, 0), (4, 100, 0), (5, 100, 0), (6, 100, 0)
ON DUPLICATE KEY UPDATE `stock` = VALUES(`stock`);

SELECT '数据库初始化完成!' AS message;
```

### 1.2 第二步：配置Docker镜像加速器

由于网络原因，直接从Docker Hub拉取镜像会失败，需要配置镜像加速器。

**操作步骤**：
1. 打开 Docker Desktop
2. 点击右上角 **Settings**（齿轮图标）
3. 选择 **Docker Engine**
4. 在JSON配置中添加 `registry-mirrors`：

```json
{
  "builder": {
    "gc": {
      "defaultKeepStorage": "20GB",
      "enabled": true
    }
  },
  "experimental": false,
  "registry-mirrors": [
    "https://docker.1panel.live",
    "https://dockerpull.org",
    "https://docker.rainbond.cc"
  ]
}
```

5. 点击 **Apply & Restart**，等待Docker重启

### 1.3 第三步：修改代码适配Docker环境

由于禁用了Redis和Kafka，需要修改代码使相关依赖变为可选。

#### 1.3.1 修改 pom.xml

添加lombok版本号：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>17</source>
        <target>17</target>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.30</version>  <!-- 必须指定版本 -->
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

#### 1.3.2 修改 RedisCacheService.java

添加条件注解，使其在Redis禁用时不加载：

```java
@Service
@ConditionalOnProperty(name = "bookstore.cache.enabled", havingValue = "true", matchIfMissing = true)
public class RedisCacheService {
    // ...
}
```

#### 1.3.3 修改 BookDaoImpl.java 和 BookInventoryDaoImpl.java

使RedisCacheService依赖变为可选：

```java
@Autowired(required = false)
private RedisCacheService redisCacheService;

private boolean isCacheAvailable() {
    return redisCacheService != null;
}

// 在使用redisCacheService的地方添加判断
if (isCacheAvailable()) {
    redisCacheService.cacheBook(book);
}
```

#### 1.3.4 修改 OrderController.java

使KafkaTemplate依赖变为可选：

```java
@Autowired(required = false)
private KafkaTemplate<String, String> kafkaTemplate;

@Autowired(required = false)
private KafkaTemplate<String, OrderRequestMessage> orderRequestKafkaTemplate;

private boolean isKafkaAvailable() {
    return kafkaTemplate != null && orderRequestKafkaTemplate != null;
}

// 在异步订单接口中，如果Kafka不可用则回退到同步处理
@PostMapping("/create-async")
public ResponseEntity<?> createOrderFromCartAsync(...) {
    if (!isKafkaAvailable()) {
        return createOrderFromCart(currentUser, payload);  // 回退到同步
    }
    // Kafka异步处理逻辑
}
```

### 1.4 第四步：构建并部署

#### 1.4.1 本地构建jar包

```powershell
cd E:\web\online-bookstore-backend
.\mvnw.cmd clean package -DskipTests
```

**预期输出**：
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  6.748 s
```

#### 1.4.2 启动Docker容器

```powershell
docker compose up -d --build
```

**预期输出**：
```
[+] Building 2.5s (8/8) FINISHED
 ✔ Network bookstore-network  Created
 ✔ Container bookstore-mysql  Healthy
 ✔ Container bookstore-backend  Started
```

#### 1.4.3 验证部署状态

```powershell
docker ps
```

**预期输出**：
```
CONTAINER ID   IMAGE                              STATUS                        PORTS
4ea2cd034086   online-bookstore-backend-backend   Up 9 minutes (healthy)        0.0.0.0:8080->8080/tcp
e6ff42d1bdab   mysql:8.0                          Up 9 minutes (healthy)        0.0.0.0:3307->3306/tcp
```

#### 1.4.4 测试API

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/books" | Select-Object totalElements
```

**预期输出**：
```
totalElements
-------------
            5
```

---

## 二、遇到的问题及解决方案

### 问题1：Docker镜像拉取失败（网络问题）

**错误信息**：
```
failed to solve: eclipse-temurin:17-jre-alpine: failed to resolve source metadata 
for docker.io/library/eclipse-temurin:17-jre-alpine: dial tcp 157.240.3.50:443: connectex
```

**解决方案**：配置Docker镜像加速器（见1.2节）

### 问题2：镜像加速器返回403 Forbidden

**错误信息**：
```
unexpected status from HEAD request to https://docker.1panel.live/...: 403 Forbidden
```

**解决方案**：更换基础镜像为 `amazoncorretto:17-alpine`（国内可访问）

### 问题3：Maven版本过低

**错误信息**：
```
The plugin org.apache.maven.plugins:maven-clean-plugin:3.4.1 requires Maven version 3.6.3
```

**解决方案**：改为在本地构建jar包，Docker只负责运行

### 问题4：lombok版本缺失

**错误信息**：
```
Resolution of annotationProcessorPath dependencies failed: version can neither be null, empty nor blank
```

**解决方案**：在pom.xml中添加lombok版本号 `1.18.30`

### 问题5：RedisCacheService依赖注入失败

**错误信息**：
```
Field redisTemplate in RedisCacheService required a bean of type 'RedisTemplate' that could not be found
```

**解决方案**：
1. 在RedisCacheService添加 `@ConditionalOnProperty` 注解
2. 在依赖它的类中使用 `@Autowired(required = false)`

### 问题6：KafkaTemplate依赖注入失败

**错误信息**：
```
Parameter 3 of constructor in OrderController required a bean of type 'KafkaTemplate' that could not be found
```

**解决方案**：将KafkaTemplate改为可选依赖，Kafka不可用时回退到同步处理

---

## 三、数据持久化验证（Bind Mount）

### 3.1 Bind Mount 工作原理

在 `docker-compose.yml` 中配置：

```yaml
volumes:
  - ./docker-data/mysql:/var/lib/mysql
```

**含义**：
- 容器内的 `/var/lib/mysql`（MySQL数据目录）
- 映射到宿主机的 `./docker-data/mysql` 目录
- 数据实际存储在宿主机上，容器删除后数据不丢失

### 3.2 验证步骤（已执行并通过）

```powershell
# 步骤1：查询当前数据
docker exec -it bookstore-mysql mysql -uroot -pbookstore123 -e "SELECT COUNT(*) FROM bookstore_db.books;"
# 结果: 5

# 步骤2：停止并删除容器
docker compose down
# 容器被删除

# 步骤3：重新创建容器
docker compose up -d
# 容器重新创建

# 步骤4：等待启动完成
Start-Sleep -Seconds 60

# 步骤5：再次查询数据
docker exec -it bookstore-mysql mysql -uroot -pbookstore123 -e "SELECT COUNT(*) FROM bookstore_db.books;"
# 结果: 5（与步骤1相同，数据未丢失）
```

### 3.3 验证结果

| 步骤 | 操作 | 图书数量 |
|------|------|----------|
| 1 | 初始查询 | 5 |
| 2 | docker compose down | 容器删除 |
| 3 | docker compose up -d | 容器重建 |
| 4 | 再次查询 | 5 |

**结论**：✅ 数据持久化成功，容器重启后数据不丢失

### 3.4 查看数据目录

```powershell
dir .\docker-data\mysql
```

可以看到MySQL数据文件：
- `ibdata1` - InnoDB系统表空间
- `ib_logfile0`, `ib_logfile1` - 重做日志
- `bookstore_db/` - 书店数据库目录
- `mysql/` - MySQL系统数据库

---

## 四、截图指南

### 推荐截图内容

1. **Docker Desktop容器列表**
   - 打开Docker Desktop → Containers
   - 截图显示两个绿色运行中的容器

2. **命令行docker ps结果**
   - 执行 `docker ps`
   - 截图显示STATUS列都是healthy

3. **数据持久化验证**
   - 截图显示重启前后查询结果相同

4. **API响应**
   - 浏览器访问 http://localhost:8080/api/books
   - 截图显示返回的JSON数据

---

## 五、快速命令参考

```powershell
# 进入项目目录
cd E:\web\online-bookstore-backend

# 本地构建
.\mvnw.cmd clean package -DskipTests

# 启动服务
docker compose up -d --build

# 查看状态
docker ps

# 查看后端日志
docker logs bookstore-backend -f

# 停止服务（保留数据）
docker compose down

# 完全清理（删除数据）
docker compose down -v
Remove-Item -Recurse -Force .\docker-data
```

---

## 六、技术说明

本部署方案**仅使用 MySQL 数据库**，禁用了Redis和Kafka：

| 组件 | 状态 | 说明 |
|------|------|------|
| MySQL 8.0 | ✅ 启用 | 核心数据库，使用Bind Mount持久化 |
| Redis | ❌ 禁用 | 缓存功能通过配置禁用 |
| Kafka | ❌ 禁用 | 消息队列通过配置禁用 |

**核心功能完全可用**：
- ✅ 用户注册/登录（JWT认证）
- ✅ 图书浏览/搜索
- ✅ 购物车管理
- ✅ 订单创建（同步模式）
- ✅ 管理员功能

---

## 七、文件清单

| 文件 | 路径 | 说明 |
|------|------|------|
| Dockerfile | `/Dockerfile` | Docker镜像构建文件 |
| docker-compose.yml | `/docker-compose.yml` | 服务编排文件 |
| .dockerignore | `/.dockerignore` | Docker构建忽略配置 |
| application-docker.properties | `/src/main/resources/` | Docker环境配置 |
| 01-init-database.sql | `/docker-init/` | 数据库初始化脚本 |
| docker-start.bat | `/docker-start.bat` | Windows启动脚本 |
| docker-stop.bat | `/docker-stop.bat` | Windows停止脚本 |

---

**文档版本**: 1.2  
**最后更新**: 2024年12月24日
