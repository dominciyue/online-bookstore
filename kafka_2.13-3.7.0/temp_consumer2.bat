@echo off
chcp 65001 >nul
title ORDER RESPONSES CONSUMER
echo [ORDER RESPONSES CONSUMER]
echo =============================================
echo Topic: order-responses
echo Encoding: UTF-8
echo.
echo Waiting for order processing results...
echo.
bin\windows\kafka-console-consumer.bat --topic order-responses --from-beginning --bootstrap-server localhost:9092
pause
