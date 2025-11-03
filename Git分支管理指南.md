# Git分支管理指南 - 微服务功能分支

## 📌 需求说明

- ✅ 创建独立的功能分支存放微服务代码
- ✅ 推送到GitHub但不与主分支合并
- ✅ 保持主分支的稳定性
- ✅ 需要时可以切换分支使用微服务功能

---

## 🌿 完整操作步骤

### 步骤1：查看当前状态

```bash
# 进入web项目目录
cd E:\web

# 查看当前分支
git branch

# 查看当前状态
git status
```

**预期输出：**
```
* main (或 master)  ← 当前在主分支
```

---

### 步骤2：创建并切换到新分支

```bash
# 方式1：创建并切换到新分支（推荐）
git checkout -b feature/microservices

# 或方式2：分两步操作
# git branch feature/microservices    # 创建分支
# git checkout feature/microservices  # 切换分支
```

**分支命名建议：**
- `feature/microservices` - 微服务功能分支
- `feature/optional-services` - 可选服务分支
- `dev/microservices` - 开发分支

**验证：**
```bash
git branch
# 输出应该显示：
# * feature/microservices  ← 带*表示当前分支
#   main
```

---

### 步骤3：添加要提交的文件

```bash
# 查看修改过的文件
git status

# 添加所有修改的文件
git add .

# 或者选择性添加
git add src/
git add online-bookstore-backend/
git add package.json
git add *.md
```

**检查将要提交的内容：**
```bash
git status
```

---

### 步骤4：提交到本地分支

```bash
# 提交修改
git commit -m "feat: 添加微服务架构支持

- 添加Author微服务（根据书名查询作者）
- 添加Price Calculator函数式服务（订单价格计算）
- 添加Eureka服务注册中心
- 添加Gateway API网关
- 集成Spring Cloud和OpenFeign
- 添加微服务详细文档

功能为可选，不影响主分支的单体应用架构"
```

---

### 步骤5：推送到GitHub

```bash
# 首次推送新分支到GitHub
git push -u origin feature/microservices

# 以后再推送只需要
# git push
```

**输出示例：**
```
Enumerating objects: 150, done.
Counting objects: 100% (150/150), done.
Delta compression using up to 8 threads
Compressing objects: 100% (80/80), done.
Writing objects: 100% (100/100), 50.00 KiB | 5.00 MiB/s, done.
Total 100 (delta 45), reused 0 (delta 0)
remote: Resolving deltas: 100% (45/45), done.
To https://github.com/your-username/your-repo.git
 * [new branch]      feature/microservices -> feature/microservices
Branch 'feature/microservices' set up to track remote branch 'feature/microservices' from 'origin'.
```

---

## 🔄 其他4个独立项目的处理

你有5个独立的项目目录，需要分别处理：

### 方式A：每个项目独立管理（推荐）

#### 1. author-service

```bash
cd E:\author-service

# 初始化Git（如果还没有）
git init

# 创建.gitignore
echo "target/" > .gitignore
echo ".idea/" >> .gitignore
echo "*.iml" >> .gitignore

# 添加所有文件
git add .

# 提交
git commit -m "feat: Author微服务 - 根据书名查询作者

- 实现精确查询和模糊查询
- 集成Eureka服务注册
- 端口: 8081"

# 关联GitHub仓库（创建新仓库）
git remote add origin https://github.com/your-username/author-service.git

# 推送
git push -u origin main
```

#### 2. eureka-server

```bash
cd E:\eureka-server

git init
echo "target/" > .gitignore
git add .
git commit -m "feat: Eureka服务注册中心

- Spring Cloud Netflix Eureka Server
- 端口: 8761
- 提供服务注册与发现功能"

git remote add origin https://github.com/your-username/eureka-server.git
git push -u origin main
```

#### 3. gateway-service

```bash
cd E:\gateway-service

git init
echo "target/" > .gitignore
git add .
git commit -m "feat: API Gateway网关服务

- Spring Cloud Gateway
- 统一入口和路由转发
- 端口: 8080
- 集成Eureka进行服务发现"

git remote add origin https://github.com/your-username/gateway-service.git
git push -u origin main
```

#### 4. price-calculator-service

```bash
cd E:\price-calculator-service

git init
echo "target/" > .gitignore
git add .
git commit -m "feat: 价格计算函数式服务

- 无状态函数式服务设计
- 支持单项和批量计算
- 端口: 8083
- 可无限水平扩展"

git remote add origin https://github.com/your-username/price-calculator-service.git
git push -u origin main
```

### 方式B：使用Git Submodules（高级）

如果想让web项目引用其他4个项目：

```bash
cd E:\web

# 在feature分支中添加子模块
git checkout feature/microservices

# 添加其他项目作为子模块
git submodule add https://github.com/your-username/author-service.git microservices/author-service
git submodule add https://github.com/your-username/eureka-server.git microservices/eureka-server
git submodule add https://github.com/your-username/gateway-service.git microservices/gateway-service
git submodule add https://github.com/your-username/price-calculator-service.git microservices/price-calculator-service

# 提交子模块配置
git add .gitmodules microservices/
git commit -m "feat: 添加微服务子模块"
git push
```

---

## 🔀 分支切换与使用

### 切换到主分支（单体应用）

```bash
cd E:\web
git checkout main

# 现在代码恢复到没有微服务的状态
# 可以正常使用单体应用
```

### 切换到微服务分支

```bash
cd E:\web
git checkout feature/microservices

# 现在可以使用微服务功能
# 启动Eureka、Gateway等服务
```

### 查看所有分支

```bash
# 本地分支
git branch

# 远程分支
git branch -r

# 所有分支
git branch -a
```

---

## 📦 GitHub上的显示

在GitHub上，你会看到：

```
Repository: your-repo
├── main (默认分支)
│   └── 单体应用代码
│
└── feature/microservices (独立分支)
    └── 微服务架构代码
    
不会自动合并！
可以独立维护！
```

---

## 🚫 避免意外合并到主分支

### 方法1：设置分支保护规则（GitHub网页）

1. 进入GitHub仓库页面
2. 点击 `Settings` → `Branches`
3. 在 `Branch protection rules` 中设置：
   - 保护 `main` 分支
   - 勾选 `Require pull request reviews before merging`
   - 这样就不会意外合并

### 方法2：本地操作注意事项

```bash
# ❌ 不要在main分支上合并feature分支
git checkout main
git merge feature/microservices  # ← 不要执行这个！

# ✅ 正确做法：保持分支独立
git checkout feature/microservices  # 在feature分支上工作
git add .
git commit -m "update"
git push  # 只推送到feature分支
```

---

## 📝 .gitignore 配置

确保每个项目都有正确的 `.gitignore`：

### web项目（React）

```gitignore
# See https://help.github.com/articles/ignoring-files/ for more about ignoring files.

# dependencies
/node_modules
/.pnp
.pnp.js

# testing
/coverage

# production
/build

# misc
.DS_Store
.env.local
.env.development.local
.env.test.local
.env.production.local

npm-debug.log*
yarn-debug.log*
yarn-error.log*

# Java backend
online-bookstore-backend/target/
online-bookstore-backend/.idea/
online-bookstore-backend/*.iml

# uploads
uploads/
```

### Java项目（通用）

```gitignore
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties

# IDE
.idea/
*.iml
*.iws
.vscode/
.classpath
.project
.settings/

# Logs
logs/
*.log

# OS
.DS_Store
Thumbs.db
```

---

## 🎯 完整工作流程总结

### 日常开发（在feature分支）

```bash
# 1. 切换到feature分支
git checkout feature/microservices

# 2. 进行开发
# ... 修改代码 ...

# 3. 查看修改
git status
git diff

# 4. 提交修改
git add .
git commit -m "feat: 添加新功能"

# 5. 推送到GitHub
git push

# 6. 如果需要使用单体应用，切换回main
git checkout main
```

### 同步主分支的更新（可选）

如果主分支有新的更新，想合并到feature分支：

```bash
# 在feature分支
git checkout feature/microservices

# 拉取main分支的最新代码
git fetch origin main

# 合并main的更新到feature（rebase方式，保持历史清晰）
git rebase origin/main

# 或使用merge方式
# git merge origin/main

# 推送
git push
```

---

## ⚠️ 注意事项

1. **不要反向合并**：不要把feature分支合并到main
2. **保持独立**：两个分支可以长期并存
3. **文档说明**：在README中说明有feature分支及其用途
4. **定期推送**：记得定期推送到GitHub备份

---

## 🔗 推荐的仓库结构

```
GitHub Organization: your-username
├── bookstore-web (主仓库)
│   ├── main (单体应用)
│   └── feature/microservices (微服务版本)
│
├── bookstore-author-service (独立仓库)
├── bookstore-eureka-server (独立仓库)
├── bookstore-gateway-service (独立仓库)
└── bookstore-price-calculator (独立仓库)
```

**优点：**
- ✅ 每个微服务独立管理
- ✅ 可以独立发布版本
- ✅ 易于团队协作
- ✅ 主仓库保持简洁

---

## 📚 相关命令速查

```bash
# 分支操作
git branch                          # 查看本地分支
git branch -a                       # 查看所有分支
git checkout <branch>               # 切换分支
git checkout -b <branch>            # 创建并切换分支
git branch -d <branch>              # 删除本地分支

# 提交操作
git add .                           # 添加所有修改
git commit -m "message"             # 提交
git push                            # 推送到远程
git push -u origin <branch>         # 首次推送新分支

# 查看状态
git status                          # 查看状态
git log --oneline --graph           # 查看提交历史
git diff                            # 查看未暂存的修改

# 远程操作
git remote -v                       # 查看远程仓库
git fetch                           # 拉取远程更新
git pull                            # 拉取并合并
```

---

**创建日期：** 2025-11-03  
**适用项目：** E-Book在线书店微服务架构

