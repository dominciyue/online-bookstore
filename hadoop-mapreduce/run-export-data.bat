@echo off
chcp 65001 >nul
echo ==========================================
echo   E-BookStore 图书数据导出工具
echo ==========================================
echo.

REM 尝试找到Maven
set MAVEN_CMD=

REM 首先检查全局Maven
where mvn >nul 2>&1
if %ERRORLEVEL% equ 0 (
    set MAVEN_CMD=mvn
    echo [信息] 使用全局Maven
    goto :build
)

REM 检查后端项目的Maven Wrapper
if exist "..\online-bookstore-backend\mvnw.cmd" (
    set MAVEN_CMD=..\online-bookstore-backend\mvnw.cmd
    echo [信息] 使用Maven Wrapper
    goto :build
)

REM 如果没有Maven，使用PowerShell直接连接数据库
echo [信息] 未找到Maven，使用PowerShell导出数据...
goto :ps_export

:build
echo [1/3] 编译项目...
call %MAVEN_CMD% compile -q
if %ERRORLEVEL% neq 0 (
    echo [错误] 编译失败，尝试使用PowerShell导出...
    goto :ps_export
)
echo      ✓ 编译成功

REM 复制依赖
echo [2/3] 复制依赖...
call %MAVEN_CMD% dependency:copy-dependencies -DoutputDirectory=target/lib -q
echo      ✓ 依赖已复制

REM 运行数据导出
echo [3/3] 运行数据导出...
echo.

REM 构建classpath
setlocal enabledelayedexpansion
set CLASSPATH=target/classes
for %%f in (target\lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%f

java -cp "!CLASSPATH!" com.bookstore.hadoop.BookDataExporter -o input %*

endlocal
goto :end

:ps_export
echo.
echo [使用PowerShell导出数据]
powershell -ExecutionPolicy Bypass -File "%~dp0export-data.ps1"

:end
echo.
echo ==========================================
echo   数据导出完成！
echo   输出目录: input/
echo ==========================================
pause
