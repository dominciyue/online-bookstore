# Redis Failover Test Script
# This script tests the system's behavior when Redis is stopped and restored

Write-Host "=== Redis Failover Test Started ===" -ForegroundColor Cyan
Write-Host ""

# 1. Test Redis Normal State
Write-Host "[1] Testing Redis normal state..." -ForegroundColor Green
curl.exe http://localhost:8080/api/books/4
Start-Sleep -Seconds 2

# 2. Test Again (Cache Hit)
Write-Host "`n[2] Testing cache hit..." -ForegroundColor Green
curl.exe http://localhost:8080/api/books/4
Start-Sleep -Seconds 2

# 3. Prompt to Stop Redis
Write-Host "`n[3] Please STOP Redis service now (Press Ctrl+C in Redis window)" -ForegroundColor Yellow
Write-Host "After stopping, press any key to continue..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# 4. Test Redis Down
Write-Host "`n[4] Testing Redis down state..." -ForegroundColor Red
curl.exe http://localhost:8080/api/books/4
Start-Sleep -Seconds 2

# 5. Multiple Tests
Write-Host "`n[5] Multiple tests to verify continuous availability..." -ForegroundColor Red
curl.exe http://localhost:8080/api/books/6
Start-Sleep -Seconds 1
curl.exe http://localhost:8080/api/books/9
Start-Sleep -Seconds 2

# 6. Prompt to Restart Redis
Write-Host "`n[6] Please RESTART Redis service now" -ForegroundColor Yellow
Write-Host "After starting, press any key to continue..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# 7. Test Redis Restored
Write-Host "`n[7] Testing Redis restored..." -ForegroundColor Green
curl.exe http://localhost:8080/api/books/4
Start-Sleep -Seconds 2

# 8. Verify Cache Rebuild
Write-Host "`n[8] Verifying cache rebuild..." -ForegroundColor Green
curl.exe http://localhost:8080/api/books/4

Write-Host "`n=== Test Completed ===" -ForegroundColor Cyan
Write-Host "Please check backend logs to confirm failover behavior!" -ForegroundColor Yellow

