@echo off
chcp 65001 >nul
echo ==========================================
echo   E-BookStore MapReduce (Hadoop Mode)
echo ==========================================
echo.

REM Check Hadoop
where hadoop >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Hadoop not found in PATH
    echo.
    echo Please install Hadoop first:
    echo   1. Download from https://hadoop.apache.org/releases.html
    echo   2. Set HADOOP_HOME environment variable
    echo   3. Add %%HADOOP_HOME%%\bin to PATH
    echo.
    echo Or use the simplified version: run-simple.bat
    pause
    exit /b 1
)

echo [INFO] Hadoop found: 
hadoop version | findstr "Hadoop"
echo.

REM Check Maven
set MAVEN_CMD=
where mvn >nul 2>&1
if %ERRORLEVEL% equ 0 (
    set MAVEN_CMD=mvn
) else if exist "..\online-bookstore-backend\mvnw.cmd" (
    set MAVEN_CMD=..\online-bookstore-backend\mvnw.cmd
) else (
    echo [ERROR] Maven not found
    pause
    exit /b 1
)

REM Build JAR
echo [1/3] Building JAR package...
call %MAVEN_CMD% package -DskipTests -q
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Build failed
    pause
    exit /b 1
)
echo       Build successful
echo.

REM Check input files
if not exist "input\*.txt" (
    echo [ERROR] No input files found in input/
    echo Please run: run-export-data.bat
    pause
    exit /b 1
)

REM Run Hadoop MapReduce
echo [2/3] Running Hadoop MapReduce job...
echo.

REM Delete output if exists
if exist "output" (
    echo Deleting existing output directory...
    rmdir /s /q output
)

hadoop jar target\bookstore-keyword-count-1.0-SNAPSHOT.jar ^
    com.bookstore.hadoop.KeywordCountDriver ^
    input output src\main\resources\keywords.txt

echo.
echo [3/3] Job completed!
echo.

REM Show results
if exist "output\part-r-00000" (
    echo ==========================================
    echo   Results (output\part-r-00000)
    echo ==========================================
    type output\part-r-00000
)

pause

