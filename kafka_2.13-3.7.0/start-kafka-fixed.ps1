Write-Host "=== Kafka Fixed Startup Script ===" -ForegroundColor Cyan

# 1. Stop Services
Write-Host "`n[1/5] Stopping Services..." -ForegroundColor Yellow
.\bin\windows\kafka-server-stop.bat 2>$null
.\bin\windows\zookeeper-server-stop.bat 2>$null
Start-Sleep -Seconds 3
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 2

# 2. Clean Data
Write-Host "`n[2/5] Cleaning Data..." -ForegroundColor Yellow
$paths = @("E:\tmp\kafka-logs", "E:\tmp\zookeeper")
foreach ($path in $paths) {
    if (Test-Path $path) {
        Write-Host "Deleting: $path" -ForegroundColor White
        Remove-Item -Path $path -Recurse -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 1
    }
}

# 3. Recreate Directories
Write-Host "`n[3/5] Recreating Directories..." -ForegroundColor Yellow
New-Item -ItemType Directory -Path "E:\tmp\kafka-logs" -Force
New-Item -ItemType Directory -Path "E:\tmp\zookeeper" -Force

# 4. Start Zookeeper
Write-Host "`n[4/5] Starting Zookeeper..." -ForegroundColor Yellow
Start-Process -FilePath ".\bin\windows\zookeeper-server-start.bat" -ArgumentList ".\config\zookeeper.properties" -NoNewWindow
Start-Sleep -Seconds 8

# Check Zookeeper
$zk = netstat -an | Select-String ":2181"
if ($zk) { 
    Write-Host "✅ Zookeeper (2181) Running" -ForegroundColor Green 
} else { 
    Write-Host "❌ Zookeeper (2181) Failed to Start" -ForegroundColor Red
    exit 1
}

# 5. Start Kafka
Write-Host "`n[5/5] Starting Kafka..." -ForegroundColor Yellow
Start-Process -FilePath ".\bin\windows\kafka-server-start.bat" -ArgumentList ".\config\server.properties" -NoNewWindow
Start-Sleep -Seconds 15

# Check Status
Write-Host "`nChecking Service Status..." -ForegroundColor Cyan
$kf = netstat -an | Select-String ":9092"

if ($kf) { 
    Write-Host "✅ Kafka (9092) Running" -ForegroundColor Green 
} else { 
    Write-Host "❌ Kafka (9092) Not Running" -ForegroundColor Red
}

if ($zk -and $kf) {
    Write-Host "`n🎉 Kafka Services Started Successfully!" -ForegroundColor Green
    Write-Host "Zookeeper: localhost:2181" -ForegroundColor White
    Write-Host "Kafka: localhost:9092" -ForegroundColor White
} else {
    Write-Host "`n⚠️ Service startup failed, please check logs" -ForegroundColor Yellow
    Write-Host "Check logs in: logs/server.log" -ForegroundColor White
}
