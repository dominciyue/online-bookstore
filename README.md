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

## 调试 WebSocket
浏览器控制台：
```js
window.__wsService?.getDebugState()
```
如需日志参数：在 URL 添加 `?debugWs=1`.
