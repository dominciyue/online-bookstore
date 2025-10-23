Write-Host "=== Kafka Full Restart Script ===" -ForegroundColor Cyan

# 1. Stop Services
Write-Host "`n[1/4] Stopping Services..." -ForegroundColor Yellow
.\bin\windows\kafka-server-stop.bat 2>$null
.\bin\windows\zookeeper-server-stop.bat 2>$null
Start-Sleep -Seconds 3
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 2

# 2. Clean Data
Write-Host "`n[2/4] Cleaning Data..." -ForegroundColor Yellow
$paths = @("E:\tmp\kafka-logs", "E:\tmp\zookeeper")
foreach ($path in $paths) {
    if (Test-Path $path) {
        Write-Host "Deleting: $path" -ForegroundColor White
        Remove-Item -Path $path -Recurse -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 1
    }
}

# 3. Recreate Directories
Write-Host "`n[3/4] Recreating Directories..." -ForegroundColor Yellow
New-Item -ItemType Directory -Path "E:\tmp\kafka-logs" -Force
New-Item -ItemType Directory -Path "E:\tmp\zookeeper" -Force

# 4. Start Services
Write-Host "`n[4/4] Starting Services..." -ForegroundColor Yellow
Start-Process -FilePath ".\bin\windows\zookeeper-server-start.bat" -ArgumentList ".\config\zookeeper.properties" -NoNewWindow
Start-Sleep -Seconds 5

Start-Process -FilePath ".\bin\windows\kafka-server-start.bat" -ArgumentList ".\config\server.properties" -NoNewWindow
Start-Sleep -Seconds 10

# Check Status
Write-Host "`nChecking Service Status..." -ForegroundColor Cyan
$zk = netstat -an | Select-String ":2181"
$kf = netstat -an | Select-String ":9092"

if ($zk) { Write-Host "✅ Zookeeper (2181) Running" -ForegroundColor Green }
else { Write-Host "❌ Zookeeper (2181) Not Running" -ForegroundColor Red }

if ($kf) { Write-Host "✅ Kafka (9092) Running" -ForegroundColor Green }
else { Write-Host "❌ Kafka (9092) Not Running" -ForegroundColor Red }

if ($zk -and $kf) {
    Write-Host "`n🎉 Kafka Services Restarted Successfully!" -ForegroundColor Green
} else {
    Write-Host "`n⚠️ Service startup may have issues, please check logs" -ForegroundColor Yellow
}