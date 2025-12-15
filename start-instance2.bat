@echo off
echo ========================================
echo Starting Bookstore Instance 2 (Port 8081)
echo Group-ID: bookstore-order-group-8081
echo ========================================
echo.
cd online-bookstore-backend
call mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--spring.config.location=classpath:/application-8081.properties
pause

