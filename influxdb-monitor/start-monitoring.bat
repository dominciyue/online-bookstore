@echo off
echo ========================================
echo   InfluxDB + Telegraf 监控系统启动
echo ========================================
echo.

cd /d "%~dp0"

echo 正在启动 InfluxDB 和 Telegraf...
docker-compose up -d

echo.
echo 等待服务启动...
timeout /t 10 /nobreak > nul

echo.
echo ========================================
echo   服务已启动！
echo ========================================
echo.
echo InfluxDB Web 界面: http://localhost:8086
echo.
echo 登录信息:
echo   用户名: admin
echo   密码:   admin123456
echo.
echo 组织:   myorg
echo Bucket: system_metrics
echo.
echo 按任意键打开浏览器访问 InfluxDB...
pause > nul
start http://localhost:8086

