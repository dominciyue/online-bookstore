@echo off
echo === Testing Kafka Log Format Fix ===

echo.
echo [1/4] Checking Kafka service...
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
echo [2/4] Checking topics...
bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092

echo.
echo [3/4] Starting message consumers...
echo Starting order-requests consumer (new window)...
start "Order Requests" cmd /k "bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic order-requests --from-beginning --property print.key=true --property print.timestamp=true"

echo Starting order-responses consumer (new window)...
timeout /t 2
start "Order Responses" cmd /k "bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic order-responses --from-beginning --property print.key=true --property print.timestamp=true"

echo.
echo [4/4] Instructions:
echo.
echo === TEST SCENARIO: Single Book Order ===
echo 1. Open browser and go to any book detail page
echo 2. Click "立即购买" (Buy Now)
echo 3. Fill shipping address
echo 4. Click "确认下单" (Confirm Order)
echo.
echo Expected backend console logs:
echo.
echo === ASYNC SINGLE ORDER REQUEST SENT ===
echo Request ID: [UUID]
echo Message JSON: {"requestId":"...","requestType":"SINGLE_BOOK_ORDER",...}  // ← JSON格式！
echo.
echo === ORDER MESSAGE LISTENER ===
echo Received message: {"requestId":"...","requestType":"SINGLE_BOOK_ORDER",...}  // ← JSON格式！
echo Parsed request message: OrderRequestMessage(requestId=..., requestType=SINGLE_BOOK_ORDER,...)
echo.
echo === ORDER RESPONSE SENT ===
echo Response message: {"requestId":"...","responseType":"SUCCESS",...}  // ← JSON格式！
echo.
echo Order Requests window: Should show JSON message
echo Order Responses window: Should show JSON response
echo.
echo === SUCCESS CRITERIA ===
echo ✅ Request message in logs is JSON format (not Java object string)
echo ✅ Response message in logs is JSON format (not Java object string)
echo ✅ OrderMessageListener parses JSON successfully
echo ✅ OrderService processes order correctly
echo ✅ Response is sent back via Kafka
echo ✅ Database shows order created
echo.
echo Press any key to close this window and keep consumer windows open...
pause >nul

