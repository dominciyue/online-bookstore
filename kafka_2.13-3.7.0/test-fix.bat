@echo off
echo === Testing Kafka Message Serialization Fix ===

echo.
echo [1/3] Checking Kafka service...
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
echo [2/3] Checking topics...
bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092

echo.
echo [3/3] Testing message format...
echo Creating a test JSON message for order-requests topic...
echo {"requestId":"test-fix-123","requestType":"SINGLE_BOOK_ORDER","userId":1,"userName":"testuser","shippingAddress":"Test Address","bookId":1,"bookTitle":"Test Book","quantity":1,"bookPrice":29.99,"timestamp":"2025-01-24T10:00:00"} | bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic order-requests --property "key=test-fix"

echo.
echo ✅ Test message sent with JSON format
echo.
echo Please check the backend console for:
echo - === ASYNC ORDER REQUEST SENT ===
echo - === ORDER MESSAGE LISTENER ===
echo - === ORDER RESPONSE SENT ===
echo.
echo If you see "com.bookstore..." instead of JSON, the fix didn't work.
echo If you see proper JSON parsing and processing, the fix is successful!
echo.
pause

