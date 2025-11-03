# 微服务模块

本目录使用Git Submodules管理各个独立的微服务项目。

## 📦 包含的微服务

| 服务名 | 端口 | 仓库地址 | 说明 |
|--------|------|----------|------|
| **author-service** | 8081 | [GitHub](https://github.com/dominciyue/author-service) | 作者查询微服务 |
| **eureka-server** | 8761 | [GitHub](https://github.com/dominciyue/eureka-server) | 服务注册中心 |
| **gateway-service** | 8080 | [GitHub](https://github.com/dominciyue/gateway-service) | API网关 |
| **price-calculator-service** | 8083 | [GitHub](https://github.com/dominciyue/price-calculator-service) | 价格计算服务 |

---

## 🚀 快速开始

### 首次克隆（包含子模块）

```bash
# 克隆主仓库并初始化所有子模块
git clone --recurse-submodules https://github.com/dominciyue/web.git

# 或者如果已经克隆了主仓库，初始化子模块
git submodule update --init --recursive
```

### 启动顺序

```bash
# 1. 启动Eureka注册中心
cd microservices/eureka-server
# 运行主类: com.bookstore.eureka.EurekaServerApplication

# 2. 启动Author微服务
cd ../author-service
# 运行主类: com.bookstore.author.AuthorServiceApplication

# 3. 启动Price Calculator
cd ../price-calculator-service
# 运行主类: com.bookstore.calculator.PriceCalculatorServiceApplication

# 4. 启动Gateway
cd ../gateway-service
# 运行主类: com.bookstore.gateway.GatewayServiceApplication

# 5. 启动主后端（返回web根目录）
cd ../..
cd online-bookstore-backend
# 运行主类: com.bookstore.online_bookstore_backend.OnlineBookstoreBackendApplication

# 6. 启动前端
cd ..
npm start
```

---

## 🔄 更新子模块

### 更新所有子模块到最新版本

```bash
git submodule update --remote
```

### 更新特定子模块

```bash
cd microservices/author-service
git pull origin main
cd ../..
git add microservices/author-service
git commit -m "chore: 更新author-service到最新版本"
```

---

## 🛠️ 开发工作流

### 修改子模块代码

```bash
# 1. 进入子模块目录
cd microservices/author-service

# 2. 创建开发分支
git checkout -b feature/new-feature

# 3. 修改代码
# ... 开发 ...

# 4. 提交到子模块仓库
git add .
git commit -m "feat: 添加新功能"
git push origin feature/new-feature

# 5. 返回主仓库
cd ../..

# 6. 更新子模块引用（可选）
git add microservices/author-service
git commit -m "chore: 更新author-service引用"
```

---

## ⚠️ 注意事项

1. **子模块是独立的Git仓库**
   - 每个子模块有自己的提交历史
   - 主仓库只记录子模块的commit SHA

2. **不要直接在子模块目录提交到主仓库**
   - 修改子模块代码后，先提交到子模块仓库
   - 然后在主仓库更新子模块引用

3. **团队协作**
   - 新成员克隆时使用 `--recurse-submodules`
   - 或克隆后执行 `git submodule update --init`

---

## 📚 相关命令

```bash
# 查看子模块状态
git submodule status

# 初始化子模块
git submodule init

# 更新子模块
git submodule update

# 更新到远程最新版本
git submodule update --remote

# 递归更新所有子模块
git submodule update --init --recursive

# 删除子模块
git submodule deinit microservices/service-name
git rm microservices/service-name
```

---

## 🔗 相关文档

- [Git Submodules官方文档](https://git-scm.com/book/en/v2/Git-Tools-Submodules)
- [微服务架构详解](../微服务架构详解_Gateway与ServiceRegistry.md)
- [微服务启动指南](../微服务启动指南.md)
- [Git分支管理指南](../Git分支管理指南.md)

---

**最后更新：** 2025-11-03  
**维护者：** Dominic

