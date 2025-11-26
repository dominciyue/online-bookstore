@echo off
echo ====================================
echo Testing Redis Connection
echo ====================================
echo.

redis-cli.exe ping

if %errorlevel% == 0 (
    echo Redis is running!
    echo.
    echo Opening Redis CLI...
    redis-cli.exe
) else (
    echo Redis is not running!
    echo Please start Redis server first.
)

pause











