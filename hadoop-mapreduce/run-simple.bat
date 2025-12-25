@echo off
chcp 65001 >nul
echo ╔══════════════════════════════════════════════════════════════╗
echo ║   E-BookStore 简化版关键词统计 (无需Maven/Hadoop)            ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

REM 检查输入目录
if not exist "input" (
    echo [警告] 输入目录 'input' 不存在
    pause
    exit /b 1
)

REM 检查输入文件
dir /b input\*.txt >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [警告] 输入目录中没有 .txt 文件
    pause
    exit /b 1
)

echo 使用PowerShell进行关键词统计...
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0run-simple-count.ps1"

pause

