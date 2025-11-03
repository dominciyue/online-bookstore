# 微服务架构详解：Gateway与Service Registry

> 以E-Book系统的Author微服务为例

## 一、架构总览

```
前端React(3000)
    ↓
Gateway网关(8080) ←→ Eureka注册中心(8761)
    ↓                      ↑
    ├→ 主后端服务(8082) ←──┤
    │   ↓ (Feign调用)
    └→ Author微服务(8081) ←┘
         ↓
    MySQL数据库(3306)
```

## 二、核心组件及作用

### 1. **Service Registry（服务注册中心）- Eureka**

**位置：** `E:\eureka-server` (端口: 8761)

**核心作用：**
- **服务注册表**：维护所有微服务的实例列表（名称、IP、端口、健康状态）
- **服务发现**：让服务消费者能找到服务提供者，无需硬编码IP地址
- **健康检查**：定期检查服务健康状态，自动剔除不可用实例

**工作原理：**
```
1. 服务启动 → 向Eureka注册（携带服务名、IP、端口）
2. Eureka存储 → 维护服务注册表
3. 服务消费者 → 从Eureka获取服务实例列表
4. 心跳机制 → 服务每5秒发送心跳，超时则标记为下线
```

**配置示例：**
```yaml
# E:\eureka-server\src\main\resources\application.yml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false  # 自己不注册
    fetch-registry: false         # 自己不拉取
```

---

### 2. **API Gateway（API网关）- Spring Cloud Gateway**

**位置：** `E:\gateway-service` (端口: 8080)

**核心作用：**
- **统一入口**：所有外部请求的唯一入口，前端只需访问一个地址
- **路由转发**：根据URL路径自动转发到对应微服务
- **负载均衡**：多个服务实例间自动分发请求（lb://服务名）
- **跨域处理**：统一CORS配置，无需每个服务单独配置
- **可扩展性**：集中添加认证、限流、日志、监控等功能

**路由配置示例：**
```yaml
# E:\gateway-service\src\main\resources\application.yml
spring:
  cloud:
    gateway:
      routes:
        # 主后端服务路由
        - id: bookstore-backend
          uri: lb://BOOKSTORE-BACKEND  # lb = LoadBalancer
          predicates:
            - Path=/api/books/**,/api/users/**
        
        # Author微服务路由
        - id: author-service
          uri: lb://AUTHOR-SERVICE
          predicates:
            - Path=/api/authors/**
```

**为什么需要Gateway？**

| 场景 | 没有Gateway | 有Gateway |
|------|-------------|-----------|
| **前端配置** | 需要配置多个服务地址 | 只需配置一个Gateway地址 |
| **跨域** | 每个服务单独配置CORS | Gateway统一配置 |
| **认证** | 每个服务重复实现认证逻辑 | Gateway集中认证，微服务免认证 |
| **服务变更** | IP/端口变更需修改前端 | 前端无感知，Gateway自动发现 |

---

## 三、完整请求流程解析

以"查询《Java编程思想》的作者"为例：

```
步骤1: 前端发起请求
  GET http://localhost:8080/api/authors/by-book?title=Java编程思想
  
步骤2: Gateway接收请求
  - 匹配路由规则：/api/authors/** → author-service
  - 从Eureka获取author-service实例列表
  - 选择健康实例：http://192.168.1.100:8081
  
步骤3: Gateway转发请求
  GET http://192.168.1.100:8081/api/authors/by-book?title=Java编程思想
  
步骤4: Author微服务处理
  - AuthorController接收请求
  - AuthorService查询数据库
  - 返回响应：{ "author": "Bruce Eckel", ... }
  
步骤5: Gateway返回前端
  - 添加CORS头
  - 记录日志
  - 返回响应给前端
```

---

## 四、服务间通信：Feign客户端

**场景：** 主后端服务(8082)需要调用Author微服务(8081)

**传统方式 vs Feign方式：**

```java
// ❌ 传统方式：手动HTTP调用
String url = "http://192.168.1.100:8081/api/authors/by-book?title=" + title;
RestTemplate restTemplate = new RestTemplate();
AuthorResponse response = restTemplate.getForObject(url, AuthorResponse.class);

// ✅ Feign方式：像调用本地方法一样
@FeignClient(name = "author-service", path = "/api/authors")
public interface AuthorServiceClient {
    @GetMapping("/by-book")
    AuthorResponseDTO getAuthorByBookTitle(@RequestParam("title") String title);
}

// 使用时
@Autowired
private AuthorServiceClient authorServiceClient;

AuthorResponseDTO response = authorServiceClient.getAuthorByBookTitle(title);
```

**Feign优势：**
- ✅ 自动服务发现（无需硬编码IP）
- ✅ 自动负载均衡
- ✅ 声明式调用（代码简洁）
- ✅ 集成熔断、重试等容错机制

---

## 五、实际部署的服务

### 服务清单

| 服务名 | 位置 | 端口 | 作用 |
|--------|------|------|------|
| **eureka-server** | `E:\eureka-server` | 8761 | 服务注册中心 |
| **gateway-service** | `E:\gateway-service` | 8080 | API网关 |
| **bookstore-backend** | `E:\web\online-bookstore-backend` | 8082 | 主后端服务 |
| **author-service** | `E:\author-service` | 8081 | 作者查询微服务 |
| **React前端** | `E:\web` | 3000 | 前端应用 |

### 启动顺序（重要！）

```
1. Eureka注册中心 (8761)  ← 必须最先启动
   ↓
2. Author微服务 (8081)     ← 独立微服务
   ↓
3. 主后端服务 (8082)       ← 依赖Eureka和Author
   ↓
4. Gateway网关 (8080)      ← 需要从Eureka获取服务列表
   ↓
5. React前端 (3000)        ← 通过Gateway访问后端
```

**VSCode启动方式：**
- 打开对应项目文件夹
- 找到主类（带`@SpringBootApplication`的类）
- 点击类上方的"Run"按钮
- 或使用F5调试启动

---

## 六、为什么要用微服务架构？

### 对比单体架构

| 维度 | 单体架构 | 微服务架构 |
|------|---------|------------|
| **部署** | 修改一行代码需重启整个应用 | 只需重启修改的服务 |
| **扩展** | 只能整体扩展 | 高负载服务独立扩展 |
| **技术栈** | 全部服务必须用同一技术 | 每个服务可选最适合的技术 |
| **开发** | 团队协作容易冲突 | 团队独立开发各自服务 |
| **故障隔离** | 一个模块崩溃导致全部不可用 | 服务隔离，局部故障不影响全局 |

### 实际案例

**Author微服务的价值：**
```
场景1: 作者查询功能需要优化算法
  → 只需修改author-service，不影响订单、购物车等功能

场景2: 作者查询访问量暴增（推荐了热门作者）
  → 只需扩展author-service实例（8081 → 8081/8082/8083）
  → Gateway自动负载均衡到多个实例

场景3: author-service暂时下线
  → 其他功能（购物车、订单）正常工作
  → 可配置熔断器返回默认响应
```

---

## 七、访问方式对比

### 方式1：直接访问微服务（不推荐）
```bash
# 前端直接访问author-service
GET http://localhost:8081/api/authors/by-book?title=Java编程思想

问题：
✗ 需要知道微服务的具体IP和端口
✗ 跨域问题需要每个服务单独配置
✗ 服务地址变更需要修改前端代码
```

### 方式2：通过Gateway访问（推荐）
```bash
# 前端统一访问Gateway
GET http://localhost:8080/api/authors/by-book?title=Java编程思想

优势：
✓ 前端只需知道Gateway地址
✓ 跨域统一配置
✓ Gateway自动路由到正确的微服务
✓ 自动负载均衡
✓ 集中日志和监控
```

### 方式3：主后端通过Feign调用（服务间通信）
```java
// 主后端服务通过Feign调用author-service
// 无需知道IP，自动从Eureka获取服务实例
authorServiceClient.getAuthorByBookTitle("Java编程思想");
```

---

## 八、关键配置汇总

### Eureka Server配置
```yaml
# E:\eureka-server\src\main\resources\application.yml
server:
  port: 8761
eureka:
  client:
    register-with-eureka: false  # 自己不注册
    fetch-registry: false
```

### Gateway配置
```yaml
# E:\gateway-service\src\main\resources\application.yml
server:
  port: 8080
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
spring:
  cloud:
    gateway:
      routes:
        - id: author-service
          uri: lb://AUTHOR-SERVICE  # 从Eureka动态获取
          predicates:
            - Path=/api/authors/**
```

### 微服务配置（Author Service）
```yaml
# E:\author-service\src\main\resources\application.yml
server:
  port: 8081
spring:
  application:
    name: author-service  # 注册到Eureka的服务名
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true  # 注册自己
    fetch-registry: true        # 获取其他服务
```

### 主后端配置（Feign Client）
```properties
# E:\web\online-bookstore-backend\src\main\resources\application.properties
spring.application.name=bookstore-backend
server.port=8082
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

```java
// Feign客户端
@FeignClient(name = "author-service", path = "/api/authors")
public interface AuthorServiceClient {
    @GetMapping("/by-book")
    AuthorResponseDTO getAuthorByBookTitle(@RequestParam("title") String title);
}
```

---

## 九、测试验证

### 1. 验证Eureka注册
访问：http://localhost:8761
查看是否显示：
- AUTHOR-SERVICE (1 instance)
- BOOKSTORE-BACKEND (1 instance)
- GATEWAY-SERVICE (1 instance)

### 2. 测试Author微服务
```bash
# 直接访问
GET http://localhost:8081/api/authors/by-book?title=Java编程思想
```

### 3. 测试Gateway路由
```bash
# 通过Gateway访问
GET http://localhost:8080/api/authors/by-book?title=Java编程思想
```

### 4. 测试前端集成
前端访问：http://localhost:3000/books
点击任意书籍的"查看作者"按钮

---

## 十、总结

### Gateway的作用
1. **统一入口** - 前端只需访问一个地址
2. **路由转发** - 自动分发请求到正确的微服务
3. **负载均衡** - 多实例自动分配请求
4. **CORS处理** - 统一跨域配置
5. **可扩展** - 集中认证、限流、日志

### Service Registry (Eureka)的作用
1. **服务注册** - 微服务启动时注册自己
2. **服务发现** - 消费者自动发现提供者
3. **健康检查** - 自动剔除不健康实例
4. **动态扩展** - 无需配置即可识别新实例

### 微服务架构的核心优势
- ✅ 服务独立部署和扩展
- ✅ 技术栈灵活选择
- ✅ 故障隔离
- ✅ 团队独立开发
- ✅ 按需扩展高负载服务

---

**文档完成日期：** 2025-11-03  
**作者：** AI Assistant  
**项目：** E-Book在线书店系统

