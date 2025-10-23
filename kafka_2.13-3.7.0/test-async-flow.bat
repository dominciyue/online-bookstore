@echo off
echo === Testing Complete Kafka Async Order Flow ===

echo.
echo [1/6] Checking Kafka service status...
netstat -an | findstr :9092 >nul
if %errorlevel% neq 0 (
    echo ❌ Kafka service is not running
    echo Please start Kafka first: start-kafka.bat
    pause
    exit /b 1
) else (
    echo ✅ Kafka service is running
)

echo.
echo [2/6] Checking Kafka topics...
bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092

echo.
echo [3/6] Creating missing topics if needed...
bin\windows\kafka-topics.bat --create --topic order-requests --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --if-not-exists
bin\windows\kafka-topics.bat --create --topic order-responses --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --if-not-exists

echo.
echo [4/6] Starting message consumers...
echo Starting order-requests consumer (new window)...
start "Order Requests" cmd /k "bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic order-requests --from-beginning --property print.key=true --property print.timestamp=true"

echo Starting order-responses consumer (new window)...
timeout /t 2
start "Order Responses" cmd /k "bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic order-responses --from-beginning --property print.key=true --property print.timestamp=true"

echo.
echo [5/6] Please test the following scenarios:
echo.
echo === TEST SCENARIO 1: Cart Order ===
echo 1. Open browser and login
echo 2. Add items to cart
echo 3. Click checkout
echo 4. Fill shipping address
echo 5. Click "确认并下单"
echo.
echo Expected logs in browser console: "=== FRONTEND: Calling async cart order ==="
echo Expected logs in backend console: "=== ASYNC ORDER REQUEST SENT ==="
echo Expected logs in "Order Requests" window: Kafka message with CART_ORDER
echo Expected logs in "Order Responses" window: Kafka response message
echo.

echo === TEST SCENARIO 2: Single Book Order ===
echo 1. Go to any book detail page
echo 2. Click "立即购买"
echo 3. Fill shipping address
echo 4. Click "确认下单"
echo.
echo Expected logs in browser console: "=== FRONTEND: Calling async single book order ==="
echo Expected logs in backend console: "=== ASYNC SINGLE ORDER REQUEST SENT ==="
echo Expected logs in "Order Requests" window: Kafka message with SINGLE_BOOK_ORDER
echo Expected logs in "Order Responses" window: Kafka response message
echo.

echo [6/6] Monitoring instructions:
echo - Check browser console for frontend logs
echo - Check backend console for Kafka processing logs
echo - Check "Order Requests" window for incoming messages
echo - Check "Order Responses" window for processing results
echo - Check database for order records
echo.

echo Press any key to close this window and keep consumer windows open for monitoring...
pause >nul

