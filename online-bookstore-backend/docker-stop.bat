@echo off
chcp 65001 >nul
echo ========================================
echo    在线书店 Docker 停止脚本
echo ========================================
echo.

echo [1/2] 停止服务...
docker compose down

echo.
echo [2/2] 服务已停止
echo.
echo 注意：MySQL 数据保存在 ./docker-data/mysql 目录
echo       下次启动时数据会自动恢复
echo.
pause

