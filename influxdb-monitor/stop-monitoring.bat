@echo off
echo 正在停止 InfluxDB 和 Telegraf...
cd /d "%~dp0"
docker-compose down
echo 服务已停止。
pause

