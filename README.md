# Online Bookstore Monorepo

## 目录结构
- src/ 前端源代码 (React / Web)
- online-bookstore-backend/ 后端 Spring Boot
- .gitignore 忽略规则

## 初始化与推送
```bash
cd /e/web
git init
git add .
git commit -m "chore: initial commit"
git branch -M main
git remote add origin https://github.com/dominciyue/online-bookstore.git
git push -u origin main
```

## 首次推送被拒绝处理（non-fast-forward）
远程已存在初始提交，直接推送被拒绝：
```bash
git fetch origin
git pull origin main --allow-unrelated-histories
# 解决冲突后
git add .
git commit -m "chore: merge remote main"
git push -u origin main
```
如需使用 rebase：
```bash
git fetch origin
git rebase origin/main
# 冲突 -> 编辑解决
git add .
git rebase --continue
git push -u origin main
```
仅在确认要覆盖远程时才：
```bash
git push -u origin main --force
```
重新克隆方式：
```bash
git clone https://github.com/dominciyue/online-bookstore.git new-repo
# 将本地文件复制进去
git add .
git commit -m "feat: import local code"
git push
```

## 前端开发
```bash
cd src
npm install   # 或 yarn
npm start     # 或 yarn start
```

## 后端开发
```bash
cd online-bookstore-backend
mvn spring-boot:run
```

## 常见后续操作
更新代码后推送：
```bash
git add .
git commit -m "feat: xxx"
git push
```

如需创建功能分支：
```bash
git checkout -b feature/xxx
# 开发
git push -u origin feature/xxx
```
