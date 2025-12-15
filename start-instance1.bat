@echo off
echo ========================================
echo Starting Bookstore Instance 1 (Port 8080)
echo Group-ID: bookstore-order-group-8080
echo ========================================
echo.
cd online-bookstore-backend
call mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--spring.config.location=classpath:/application-8080.properties
pause

