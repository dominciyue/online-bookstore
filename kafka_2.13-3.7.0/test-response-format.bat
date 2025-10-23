@echo off
echo === Testing OrderResponseMessage JSON Serialization ===

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
echo [3/3] Testing response message format...
echo Creating a test response message for order-responses topic...
echo {"requestId":"test-response-123","responseType":"SUCCESS","orderId":"test-order-456","userId":1,"userName":"testuser","status":"PENDING","totalAmount":"99.99","shippingAddress":"Test Address","message":"订单处理成功","timestamp":"2025-01-24T20:38:18"} | bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic order-responses --property "key=test-response"

echo.
echo ✅ Test response message sent with JSON format
echo.
echo Please check the backend console for:
echo - === ORDER RESPONSE SENT ===
echo - Response message: {JSON format}  (not Java object string)
echo.
echo Expected format:
echo Response message: {"requestId":"test-response-123","responseType":"SUCCESS",...}
echo.
echo If you see "com.bookstore..." format, the toString() method is still not working.
echo If you see proper JSON format, the fix is successful!
echo.
pause

