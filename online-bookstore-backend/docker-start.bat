@echo off
chcp 65001 >nul
echo ========================================
echo    在线书店 Docker 部署启动脚本
echo ========================================
echo.

:: 检查 Docker 是否运行
docker info >nul 2>&1
if errorlevel 1 (
    echo [错误] Docker 未运行！请先启动 Docker Desktop。
    pause
    exit /b 1
)

echo [1/3] 检查 Docker 环境...
docker --version
docker compose version
echo.

echo [2/3] 构建并启动服务...
docker compose up -d --build

echo.
echo [3/3] 等待服务启动...
echo 请稍候，MySQL 需要约 30 秒初始化...
timeout /t 30 /nobreak >nul

echo.
echo ========================================
echo    服务启动完成！
echo ========================================
echo.
echo 后端 API: http://localhost:8080/api/books
echo MySQL:    localhost:3307 (用户: root, 密码: bookstore123)
echo.
echo 查看日志: docker compose logs -f
echo 停止服务: docker compose down
echo.
pause

