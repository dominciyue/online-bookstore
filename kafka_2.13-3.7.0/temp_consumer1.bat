@echo off
chcp 65001 >nul
title ORDER REQUESTS CONSUMER
echo [ORDER REQUESTS CONSUMER]
echo =============================================
echo Topic: order-requests
echo Encoding: UTF-8
echo.
echo Waiting for incoming order requests...
echo.
bin\windows\kafka-console-consumer.bat --topic order-requests --from-beginning --bootstrap-server localhost:9092
pause
