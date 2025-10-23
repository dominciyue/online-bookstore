@echo off
chcp 65001 >nul
title TEST PRODUCER
echo [TEST PRODUCER]
echo =============================================
echo Topic: order-requests
echo Encoding: UTF-8
echo.
echo Test Messages (Ready to test with database):
echo.
echo === SUCCESS TEST (User 3 has cart items) ===
echo {"requestId":"success-test-001","requestType":"CART_ORDER","userId":3,"userName":"testfrompostman","shippingAddress":"Test Address 123"}
echo.
echo === SINGLE BOOK TEST ===
echo {"requestId":"single-test-001","requestType":"SINGLE_BOOK_ORDER","userId":1,"userName":"testuser","bookId":2,"quantity":1,"shippingAddress":"Test Address"}
echo.
echo === ERROR TEST (Empty cart user) ===
echo {"requestId":"error-test-001","requestType":"CART_ORDER","userId":1,"userName":"testuser","shippingAddress":"Error Test"}
echo.
echo Instructions:
echo 1. Copy a test message
echo 2. Paste after > prompt
echo 3. Press Enter
echo.
bin\windows\kafka-console-producer.bat --topic order-requests --bootstrap-server localhost:9092
pause
